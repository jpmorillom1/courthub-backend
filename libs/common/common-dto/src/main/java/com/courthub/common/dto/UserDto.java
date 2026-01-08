package com.courthub.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public class UserDto {
    private UUID id;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String faculty;
    
    @NotNull(message = "Roles are required")
    private Set<String> roles;

    public UserDto() {
    }

    public UserDto(UUID id, String email, String name, String faculty, Set<String> roles) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.faculty = faculty;
        this.roles = roles;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}

