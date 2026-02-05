package com.courthub.auth.service;

import com.courthub.auth.client.UserServiceFeignClient;
import com.courthub.auth.entity.RefreshToken;
import com.courthub.auth.repository.RefreshTokenRepository;
import com.courthub.common.dto.AuthRequestDto;
import com.courthub.common.dto.AuthResponseDto;
import com.courthub.common.dto.CreateUserDto;
import com.courthub.common.dto.UserDto;
import com.courthub.common.dto.ValidateCredentialsDto;
import com.courthub.common.exception.NotFoundException;
import com.courthub.common.exception.UnauthorizedException;
import com.courthub.common.security.JwtTokenProvider;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final UserServiceFeignClient userServiceFeignClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthService(
            UserServiceFeignClient userServiceFeignClient,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            RefreshTokenRepository refreshTokenRepository) {
        this.userServiceFeignClient = userServiceFeignClient;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthResponseDto login(AuthRequestDto request) {
        log.info("Authenticating user with credentials");
        Map<String, Object> validationResult = userServiceFeignClient.validateCredentials(
            new ValidateCredentialsDto(request.getEmail(), request.getPassword())
        );
        
        boolean isValid = validationResult != null && Boolean.TRUE.equals(validationResult.get("valid"));
        
        if (!isValid) {
            log.warn("Authentication failed: invalid credentials");
            throw new UnauthorizedException("Invalid email or password");
        }

        UserDto user = getUserByEmailSafely(request.getEmail());
        
        if (user == null) {
            log.warn("Authentication failed: user not found");
            throw new UnauthorizedException("User not found");
        }
        
        List<String> roles = user.getRoles() != null ? user.getRoles().stream().toList() : List.of("USER");
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        saveRefreshToken(user.getId(), refreshToken);
        log.info("Authentication successful: userId={}", user.getId());
        return new AuthResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponseDto refreshToken(String refreshTokenString) {
        log.info("Refreshing access token");
        if (!jwtTokenProvider.isRefreshToken(refreshTokenString)) {
            log.warn("Refresh token rejected: invalid token type");
            throw new UnauthorizedException("Invalid refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshTokenString);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            log.warn("Refresh token rejected: token expired");
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        UserDto user = getUserByIdSafely(userId);
        if (user == null) {
            log.warn("Refresh token failed: user not found userId={}", userId);
            throw new NotFoundException("User", userId);
        }

        List<String> roles = user.getRoles() != null ? user.getRoles().stream().toList() : List.of("USER");
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), roles);

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        saveRefreshToken(user.getId(), newRefreshToken);
        refreshTokenRepository.delete(refreshToken);
        log.info("Refresh token completed successfully: userId={}", userId);
        return new AuthResponseDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public AuthResponseDto oauth2Login(OAuth2User oauth2User) {
        log.info("Processing OAuth2 login");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null) {
            log.warn("OAuth2 login failed: email not provided by provider");
            throw new UnauthorizedException("Email not provided by OAuth2 provider");
        }

        UserDto user = null;

        try {
            user = getUserByEmailSafely(email);
        } catch (Exception e) {
            log.info("OAuth2 user not found, creating new account");
        }

        if (user == null) {
            CreateUserDto createUserDto = new CreateUserDto(
                    email,
                    null,
                    name != null ? name : email,
                    null,
                    java.util.Set.of("USER")
            );
            user = userServiceFeignClient.createUser(createUserDto);
        }

        List<String> roles = user.getRoles() != null ? user.getRoles().stream().toList() : List.of("USER");
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        saveRefreshToken(user.getId(), refreshToken);
        log.info("OAuth2 login successful: userId={}", user.getId());
        return new AuthResponseDto(accessToken, refreshToken);
    }
    
    private UserDto getUserByEmailSafely(String email) {
        try {
            return userServiceFeignClient.getUserByEmail(email);
        } catch (FeignException.NotFound e) {
            return null;
        }
    }
    
    private UserDto getUserByIdSafely(UUID userId) {
        try {
            return userServiceFeignClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            return null;
        }
    }
    
    private void saveRefreshToken(UUID userId, String token) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElse(null);

        if (refreshToken != null) {
            refreshToken.setToken(token);
            refreshToken.setExpiresAt(expiresAt);
        } else {
            refreshToken = new RefreshToken(userId, token, expiresAt);
        }

        refreshTokenRepository.save(refreshToken);
    }
}
