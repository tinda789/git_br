package com.app.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    
    @Size(max = 20, message = "Employee ID cannot exceed 20 characters")
    private String employeeId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Company ID is required")
    private Long companyId;
    
    private LocalDate hireDate;
    
    private LocalDate terminationDate;
    
    private int status = 1;
    
    private Long managerId;
    
    @Size(max = 100, message = "Job title cannot exceed 100 characters")
    private String jobTitle;
    
    @Email(message = "Work email should be valid")
    @Size(max = 100, message = "Work email cannot exceed 100 characters")
    private String workEmail;
    
    @Size(max = 20, message = "Work phone cannot exceed 20 characters")
    private String workPhone;
    
    private Set<Long> departmentIds = new HashSet<>();
    
    private Set<Long> teamIds = new HashSet<>();
    
    private Set<Long> positionIds = new HashSet<>();
}