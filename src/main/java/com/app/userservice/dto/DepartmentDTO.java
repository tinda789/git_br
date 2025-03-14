package com.app.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private Long id;
    
    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @Size(max = 20, message = "Code cannot exceed 20 characters")
    private String code;
    
    @NotNull(message = "Company ID is required")
    private Long companyId;
    
    private Long parentId;
}