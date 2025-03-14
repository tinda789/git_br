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
public class TeamDTO {
    private Long id;
    
    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @NotNull(message = "Department ID is required")
    private Long departmentId;
    
    private boolean active = true;
}