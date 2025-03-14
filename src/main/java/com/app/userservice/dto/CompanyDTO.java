package com.app.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {
    private Long id;
    
    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String name;
    
    private String logo;
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;
    
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;
    
    @Size(max = 100, message = "Website cannot exceed 100 characters")
    private String website;
    
    @Size(max = 50, message = "Tax code cannot exceed 50 characters")
    private String taxCode;
    
    @Size(max = 50, message = "Business code cannot exceed 50 characters")
    private String businessCode;
    
    private LocalDate establishedDate;
    
    private boolean active = true;
}