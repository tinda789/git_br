package com.app.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    
    @NotBlank(message = "Tên vai trò không được để trống")
    @Size(min = 3, max = 50, message = "Tên vai trò phải từ 3 đến 50 ký tự")
    private String name;
    
    @Size(max = 200, message = "Mô tả không được vượt quá 200 ký tự")
    private String description;
    
    private boolean isSystem;
}