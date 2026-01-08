package com.courthub.auth.controller;

import com.courthub.auth.service.AuthService;
import com.courthub.common.dto.AuthRequestDto;
import com.courthub.common.dto.AuthResponseDto;
import com.courthub.common.dto.RefreshTokenRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for authentication and authorization")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password", description = "Authenticates a user with email and password, returns access token and refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        AuthResponseDto response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/oauth2/login")
    @GetMapping("/oauth2/login")
    @Operation(summary = "Login with OAuth2", description = "Endpoint that processes OAuth2 authentication and returns JWT tokens. Called automatically after OAuth2 authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OAuth2 login successful"),
            @ApiResponse(responseCode = "401", description = "OAuth2 authentication failed")
    })
    public ResponseEntity<AuthResponseDto> oauth2Login(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("OAuth2 authentication required");
        }
        AuthResponseDto response = authService.oauth2Login(oauth2User);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/oauth2/error")
    @Operation(summary = "OAuth2 Error", description = "Endpoint redirected to if OAuth2 authentication fails")
    public ResponseEntity<com.courthub.common.dto.ErrorResponseDto> oauth2Error() {
        com.courthub.common.dto.ErrorResponseDto error = new com.courthub.common.dto.ErrorResponseDto(
                401,
                "OAuth2 Authentication Failed",
                "OAuth2 authentication failed. Please try again.",
                "/auth/oauth2/error"
        );
        return ResponseEntity.status(401).body(error);
    }
}
