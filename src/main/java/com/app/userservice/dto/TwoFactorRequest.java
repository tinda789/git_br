package com.app.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TwoFactorRequest {
    @NotBlank(message = "Mã xác thực không được để trống")
    @Pattern(regexp = "^[0-9]{6}$", message = "Mã xác thực phải là 6 chữ số")
    private String verificationCode;
}