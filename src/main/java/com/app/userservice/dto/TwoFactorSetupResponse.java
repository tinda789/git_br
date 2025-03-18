package com.app.userservice.dto;

import lombok.Data;

@Data
public class TwoFactorSetupResponse {
    private String secretKey;
    private String otpAuthURL;
    
    public TwoFactorSetupResponse(String secretKey, String otpAuthURL) {
        this.secretKey = secretKey;
        this.otpAuthURL = otpAuthURL;
    }
}