package com.app.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;
    
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;
    
    private String profileImage;
}