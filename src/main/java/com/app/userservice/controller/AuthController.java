package com.app.userservice.controller;

import com.app.userservice.dto.*;
import com.app.userservice.entity.user.User;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.service.AuthService;
import com.app.userservice.service.TwoFactorAuthService;
import com.app.userservice.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private VerificationService verificationService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    @Value("${app.name}")
    private String appName;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody TwoFactorRequest twoFactorRequest) {
        return ResponseEntity.ok(authService.verifyOtp(twoFactorRequest.getVerificationCode()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        MessageResponse response = authService.registerUser(signUpRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        MessageResponse response = verificationService.verifyEmail(token);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody EmailRequest emailRequest) {
        MessageResponse response = authService.resendVerificationEmail(emailRequest.getEmail());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody EmailRequest emailRequest) {
        MessageResponse response = authService.forgotPassword(emailRequest.getEmail());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetRequest) {
        MessageResponse response = authService.resetPassword(resetRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        MessageResponse response = verificationService.validatePasswordResetToken(token);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 2FA endpoints
    
    @PostMapping("/2fa/setup")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> setupTwoFactor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Map<String, String> setupInfo = authService.setupTwoFactor(userDetails.getId(), appName);
        
        return ResponseEntity.ok(new TwoFactorSetupResponse(setupInfo.get("secretKey"), setupInfo.get("otpAuthURL")));
    }
    
    @PostMapping("/2fa/enable")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> enableTwoFactor(@Valid @RequestBody TwoFactorRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = twoFactorAuthService.enableTwoFactor(userDetails.getId(), request.getVerificationCode());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/2fa/disable")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> disableTwoFactor(@Valid @RequestBody TwoFactorRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = twoFactorAuthService.disableTwoFactor(userDetails.getId(), request.getVerificationCode());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}