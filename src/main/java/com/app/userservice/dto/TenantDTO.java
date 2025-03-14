package com.app.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {
    private Long id;
    
    @NotBlank(message = "Tenant name is required")
    @Size(min = 3, max = 50, message = "Tenant name must be between 3 and 50 characters")
    private String name;
    
    @NotBlank(message = "Schema name is required")
    @Size(min = 3, max = 30, message = "Schema name must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Schema name can only contain lowercase letters, numbers and underscores")
    private String schema;
    
    @Size(max = 100, message = "Description cannot exceed 100 characters")
    private String description;
    
    private boolean active = true;
}