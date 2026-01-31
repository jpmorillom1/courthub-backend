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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        Map<String, Object> validationResult = userServiceFeignClient.validateCredentials(
            new ValidateCredentialsDto(request.getEmail(), request.getPassword())
        );
        
        boolean isValid = validationResult != null && Boolean.TRUE.equals(validationResult.get("valid"));
        
        if (!isValid) {
            throw new UnauthorizedException("Invalid email or password");
        }

        UserDto user = getUserByEmailSafely(request.getEmail());
        
        if (user == null) {
            throw new UnauthorizedException("User not found");
        }
        
        List<String> roles = user.getRoles() != null ? user.getRoles().stream().toList() : List.of("USER");
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponseDto refreshToken(String refreshTokenString) {
        if (!jwtTokenProvider.isRefreshToken(refreshTokenString)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshTokenString);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        UserDto user = getUserByIdSafely(userId);
        if (user == null) {
            throw new NotFoundException("User", userId);
        }

        List<String> roles = user.getRoles() != null ? user.getRoles().stream().toList() : List.of("USER");
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), roles);

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        saveRefreshToken(user.getId(), newRefreshToken);
        refreshTokenRepository.delete(refreshToken);

        return new AuthResponseDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public AuthResponseDto oauth2Login(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null) {
            throw new UnauthorizedException("Email not provided by OAuth2 provider");
        }

        UserDto user = null;

        try {
            user = getUserByEmailSafely(email);
        } catch (Exception e) {
            System.out.println("El usuario no existe, se procederá a crearlo: " + email);
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
