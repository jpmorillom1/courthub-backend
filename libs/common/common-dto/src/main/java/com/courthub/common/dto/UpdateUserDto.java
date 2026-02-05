package com.courthub.common.dto;

import jakarta.validation.constraints.Email;

import java.util.Set;

public class UpdateUserDto {
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String name;
    
    private String faculty;
    
    private Set<String> roles;

    public UpdateUserDto() {
    }

    public UpdateUserDto(String email, String name, String faculty, Set<String> roles) {
        this.email = email;
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

