package com.app.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDTO {
    private Long id;
    
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    private String username;
    
    @NotBlank(message = "Email không được để trống")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Email(message = "Email phải đúng định dạng")
    private String email;
    
    private String password;
    
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;
    
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;
    
    private String profileImage;
    
    private int status;
    
    private boolean emailVerified;
    
    private boolean twoFactorEnabled;
    
    private List<String> roles;
    
    private boolean accountNonLocked;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastLoginAt;
    
    private LocalDateTime lastActiveAt;
}