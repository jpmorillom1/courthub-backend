package com.courthub.user.controller;

import com.courthub.common.dto.CreateUserDto;
import com.courthub.common.dto.UpdateUserDto;
import com.courthub.common.dto.UserDto;
import com.courthub.common.dto.ValidateCredentialsDto;
import com.courthub.user.config.JwtAuthenticationToken;
import com.courthub.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create user (Registration)", description = "Creates a new user in the system. This is the public registration endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or email already exists")
    })
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        log.info("Create user request received");
        UserDto user = userService.createUser(createUserDto);
        log.info("User created successfully: userId={}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns user information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        log.info("Get user by id request received: userId={}", id);
        UserDto user = userService.getUserById(id);
        log.info("User returned successfully: userId={}", id);
        return ResponseEntity.ok(user);
    }

 
    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", description = "Returns the information of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserDto> getCurrentUser() {
        log.info("Get current user request received");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        UUID userId = jwtAuth.getUserId();
        
        UserDto user = userService.getUserById(userId);
        log.info("Current user returned successfully: userId={}", userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Returns user information by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        log.info("Get user by email request received");
        UserDto user = userService.getUserByEmail(email);
        log.info("User returned successfully by email");
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates information of an existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        log.info("Update user request received: userId={}", id);
        UserDto user = userService.updateUser(id, updateUserDto);
        log.info("User updated successfully: userId={}", id);
        return ResponseEntity.ok(user);
    }
    @PostMapping("/validate-credentials")
    @Operation(summary = "Validate credentials", description = "Validates if an email and password are correct. Used internally by auth-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    public ResponseEntity<Map<String, Object>> validateCredentials(
            @Valid @RequestBody ValidateCredentialsDto request) {
        log.info("Validate credentials request received");
        boolean isValid = userService.validateCredentials(request.getEmail(), request.getPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        log.info("Validate credentials completed");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/users/all")
    @Operation(summary = "Get all users (internal)", description = "Retrieve all users for analytics purposes - Internal endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All users returned")
    })
    public ResponseEntity<java.util.List<com.courthub.common.dto.analytics.UserInternalDTO>> getAllUsers() {
        log.info("Get all users (internal) request received");
        java.util.List<com.courthub.common.dto.analytics.UserInternalDTO> users = userService.getAllUsersForAnalytics();
        log.info("All users returned (internal): count={}", users.size());
        return ResponseEntity.ok(users);
    }
}
