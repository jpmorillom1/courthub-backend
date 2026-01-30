package com.courthub.user.service;

import com.courthub.common.dto.CreateUserDto;
import com.courthub.common.dto.UpdateUserDto;
import com.courthub.common.dto.UserDto;
import com.courthub.common.exception.BusinessException;
import com.courthub.common.exception.NotFoundException;
import com.courthub.user.domain.Role;
import com.courthub.user.entity.User;
import com.courthub.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            throw new BusinessException("User with email " + createUserDto.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(createUserDto.getEmail());
        user.setName(createUserDto.getName());
        user.setFaculty(createUserDto.getFaculty());
        
        if (createUserDto.getPassword() != null && !createUserDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        }
        
        Set<Role> roles = createUserDto.getRoles() != null 
                ? createUserDto.getRoles().stream()
                    .map(roleStr -> {
                        try {
                            return Role.valueOf(roleStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new BusinessException("Invalid role: " + roleStr);
                        }
                    })
                    .collect(Collectors.toSet())
                : Set.of(Role.USER);
        
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        return toDto(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));
        return toDto(user);
    }

    public boolean validateCredentials(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            return false;
        }
        
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    public UserDto updateUser(UUID id, UpdateUserDto updateUserDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateUserDto.getEmail())) {
                throw new BusinessException("User with email " + updateUserDto.getEmail() + " already exists");
            }
            user.setEmail(updateUserDto.getEmail());
        }

        if (updateUserDto.getName() != null) {
            user.setName(updateUserDto.getName());
        }

        if (updateUserDto.getFaculty() != null) {
            user.setFaculty(updateUserDto.getFaculty());
        }

        if (updateUserDto.getRoles() != null) {
            Set<Role> roles = updateUserDto.getRoles().stream()
                    .map(roleStr -> {
                        try {
                            return Role.valueOf(roleStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new BusinessException("Invalid role: " + roleStr);
                        }
                    })
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        return toDto(updatedUser);
    }

    private UserDto toDto(User user) {
        Set<String> roleStrings = user.getRoles().stream()
                .map(role -> role.name())
                .collect(Collectors.toSet());
        
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getFaculty(),
                roleStrings
        );
    }

    public java.util.List<com.courthub.common.dto.analytics.UserInternalDTO> getAllUsersForAnalytics() {
        return userRepository.findAll().stream()
            .map(user -> new com.courthub.common.dto.analytics.UserInternalDTO(
                user.getId().toString(),
                user.getName(),
                user.getFaculty()
            ))
            .collect(Collectors.toList());
    }
}
