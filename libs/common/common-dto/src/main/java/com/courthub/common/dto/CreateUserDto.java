package com.courthub.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public class CreateUserDto {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    private String password;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String faculty;
    
    @NotNull(message = "Roles are required")
    private Set<String> roles;

    public CreateUserDto() {
    }

    public CreateUserDto(String email, String password, String name, String faculty, Set<String> roles) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.faculty = faculty;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

