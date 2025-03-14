package com.app.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantSignupRequest {
    // Tenant information
    @NotBlank(message = "Company name is required")
    @Size(min = 3, max = 50, message = "Company name must be between 3 and 50 characters")
    private String companyName;
    
    @Size(max = 100, message = "Company description cannot exceed 100 characters")
    private String companyDescription;
    
    @NotBlank(message = "Schema name is required")
    @Size(min = 3, max = 30, message = "Schema name must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Schema name can only contain lowercase letters, numbers and underscores")
    private String schema;
    
    // Admin user information
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;
    
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;
}