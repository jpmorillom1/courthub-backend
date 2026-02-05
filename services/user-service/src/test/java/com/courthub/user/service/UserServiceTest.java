package com.courthub.user.service;

import com.courthub.common.dto.CreateUserDto;
import com.courthub.common.dto.UpdateUserDto;
import com.courthub.common.dto.UserDto;
import com.courthub.common.exception.BusinessException;
import com.courthub.common.exception.NotFoundException;
import com.courthub.user.domain.Role;
import com.courthub.user.entity.User;
import com.courthub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserDto createUserDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setFaculty("Engineering");
        testUser.setPassword("encodedPassword123");
        testUser.setRoles(Set.of(Role.USER));

        createUserDto = new CreateUserDto();
        createUserDto.setEmail("newuser@example.com");
        createUserDto.setName("New User");
        createUserDto.setFaculty("Medicine");
        createUserDto.setPassword("password123");
        createUserDto.setRoles(Set.of("USER"));
    }

    @Test
    @DisplayName("Should create user successfully with provided password")
    void testCreateUserWithValidData() {
        // Arrange
        when(userRepository.existsByEmail(createUserDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = userService.createUser(createUserDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getName()).isEqualTo(testUser.getName());
        assertThat(result.getFaculty()).isEqualTo(testUser.getFaculty());

        verify(userRepository, times(1)).existsByEmail(createUserDto.getEmail());
        verify(passwordEncoder, times(1)).encode(createUserDto.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when email already exists")
    void testCreateUserWithExistingEmail() {
        // Arrange
        when(userRepository.existsByEmail(createUserDto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, times(1)).existsByEmail(createUserDto.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should create user with random password when password is null")
    void testCreateUserWithNullPassword() {
        // Arrange
        createUserDto.setPassword(null);
        when(userRepository.existsByEmail(createUserDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedRandomPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = userService.createUser(createUserDto);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    @DisplayName("Should create user with default USER role when roles not provided")
    void testCreateUserWithDefaultRole() {
        // Arrange
        createUserDto.setRoles(null);
        when(userRepository.existsByEmail(createUserDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = userService.createUser(createUserDto);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when invalid role is provided")
    void testCreateUserWithInvalidRole() {
        // Arrange
        createUserDto.setRoles(Set.of("INVALID_ROLE"));
        when(userRepository.existsByEmail(createUserDto.getEmail())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid role");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserByIdSuccess() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = userService.getUserById(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getId()).isEqualTo(testUser.getId());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when user by ID not found")
    void testGetUserByIdNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void testGetUserByEmailSuccess() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = userService.getUserByEmail(testUser.getEmail());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("Should throw NotFoundException when user by email not found")
    void testGetUserByEmailNotFound() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByEmail(nonExistentEmail))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not found");

        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Should validate credentials successfully with correct password")
    void testValidateCredentialsSuccess() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        // Act
        boolean result = userService.validateCredentials(testUser.getEmail(), "password123");

        // Assert
        assertThat(result).isTrue();

        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
    }

    @Test
    @DisplayName("Should return false when password is incorrect")
    void testValidateCredentialsWithIncorrectPassword() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        // Act
        boolean result = userService.validateCredentials(testUser.getEmail(), "wrongPassword");

        // Assert
        assertThat(result).isFalse();

        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(passwordEncoder, times(1)).matches("wrongPassword", testUser.getPassword());
    }

    @Test
    @DisplayName("Should return false when user does not exist")
    void testValidateCredentialsUserNotFound() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.validateCredentials(nonExistentEmail, "password123");

        // Assert
        assertThat(result).isFalse();

        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should update user successfully with new email")
    void testUpdateUserSuccess() {
        // Arrange
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail("newemail@example.com");
        updateUserDto.setName("Updated Name");
        updateUserDto.setFaculty("Computer Science");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail("newemail@example.com");
        updatedUser.setName("Updated Name");
        updatedUser.setFaculty("Computer Science");
        updatedUser.setPassword("encodedPassword123");
        updatedUser.setRoles(Set.of(Role.USER));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserDto result = userService.updateUser(userId, updateUserDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("newemail@example.com");
        assertThat(result.getName()).isEqualTo("Updated Name");

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent user")
    void testUpdateUserNotFound() {
        // Arrange
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setName("Updated Name");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(userId, updateUserDto))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when updating to email that already exists")
    void testUpdateUserWithExistingEmail() {
        // Arrange
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail("existing@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(userId, updateUserDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
