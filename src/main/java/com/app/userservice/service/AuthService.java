package com.app.userservice.service;

import com.app.userservice.dto.*;
import com.app.userservice.entity.user.Role;
import com.app.userservice.entity.user.User;
import com.app.userservice.entity.user.UserVerificationToken;
import com.app.userservice.repository.RoleRepository;
import com.app.userservice.repository.UserRepository;
import com.app.userservice.repository.UserVerificationTokenRepository;
import com.app.userservice.security.jwt.JwtUtils;
import com.app.userservice.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private VerificationService verificationService;
    
    @Autowired
    private UserVerificationTokenRepository tokenRepository;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    // Temporary storage for 2FA authentication
    private Map<String, String> twoFactorAuthenticationTokens = new HashMap<>();

    public Map<String, Object> authenticateUser(LoginRequest loginRequest) {
        // Authenticate with username and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Check if 2FA is enabled
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user != null && user.isTwoFactorEnabled()) {
            // Generate temporary token for 2FA
            String tempToken = java.util.UUID.randomUUID().toString();
            twoFactorAuthenticationTokens.put(tempToken, user.getUsername());
            
            // Return temp token and indicate 2FA is required
            Map<String, Object> response = new HashMap<>();
            response.put("requires2FA", true);
            response.put("tempToken", tempToken);
            return response;
        }
        
        // If 2FA is not enabled, proceed with normal authentication
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // Update last login time
        if (user != null) {
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastActiveAt(LocalDateTime.now());
            userRepository.save(user);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userDetails);
        response.put("requires2FA", false);
        return response;
    }
    
    public Map<String, Object> verifyOtp(String verificationCode) {
        // Get current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new RuntimeException("Không thể xác thực");
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        // Verify OTP
        boolean isValid = twoFactorAuthService.validateOTP(user, verificationCode);
        
        if (!isValid) {
            throw new RuntimeException("Mã OTP không hợp lệ");
        }
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userDetails);
        
        return response;
    }

    @Transactional
    public MessageResponse registerUser(SignupRequest signUpRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!", false);
        }

        // Check if email exists
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!", false);
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setFullName(signUpRequest.getFullName());
        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setStatus(1); // Active
        user.setEmailVerified(true); // Đặt true để không cần xác thực email trong quá trình test
        user.setCreatedAt(LocalDateTime.now());
        
        // Assign USER role by default
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
        roles.add(userRole);
        
        user.setRoles(roles);
        userRepository.save(user);
        
        // Gửi email xác thực - bắt và xử lý ngoại lệ
        try {
            verificationService.sendVerificationEmail(user);
        } catch (Exception e) {
            // Ghi log lỗi nhưng không ảnh hưởng đến kết quả đăng ký
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        return new MessageResponse("User registered successfully! Please check your email for verification.", true);
    }
    
    @Transactional
    public MessageResponse resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        
        if (user.isEmailVerified()) {
            return new MessageResponse("Email đã được xác thực", false);
        }
        
        // Bắt và xử lý ngoại lệ khi gửi lại email
        try {
            verificationService.sendVerificationEmail(user);
        } catch (Exception e) {
            System.err.println("Failed to resend verification email: " + e.getMessage());
            // Trả về thông báo thành công dù có lỗi (để tránh lộ thông tin)
        }
        
        return new MessageResponse("Email xác thực đã được gửi lại", true);
    }
    
    @Transactional
    public MessageResponse forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        // Không thông báo nếu email không tồn tại (bảo mật)
        if (user != null) {
            try {
                verificationService.sendPasswordResetEmail(email);
            } catch (Exception e) {
                System.err.println("Failed to send password reset email: " + e.getMessage());
                // Không ném ngoại lệ ra ngoài
            }
        }
        
        return new MessageResponse("Nếu email tồn tại trong hệ thống, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu", true);
    }
    
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest resetRequest) {
        // Kiểm tra token
        UserVerificationToken resetToken = tokenRepository.findByTokenAndTokenType(resetRequest.getToken(), "PASSWORD_RESET")
                .orElse(null);
        
        if (resetToken == null) {
            return new MessageResponse("Token không hợp lệ", false);
        }
        
        if (resetToken.isExpired()) {
            return new MessageResponse("Token đã hết hạn", false);
        }
        
        if (resetToken.isUsed()) {
            return new MessageResponse("Token đã được sử dụng", false);
        }
        
        // Kiểm tra mật khẩu xác nhận
        if (!resetRequest.getNewPassword().equals(resetRequest.getConfirmPassword())) {
            return new MessageResponse("Mật khẩu mới và xác nhận mật khẩu không khớp", false);
        }
        
        // Đặt lại mật khẩu
        User user = resetToken.getUser();
        user.setPassword(encoder.encode(resetRequest.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Đánh dấu token đã sử dụng
        resetToken.setUsed(true);
        resetToken.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);
        
        return new MessageResponse("Mật khẩu đã được đặt lại thành công", true);
    }
    
    @Transactional
    public Map<String, String> setupTwoFactor(Long userId, String appName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        return twoFactorAuthService.generateSecretKey(user, appName);
    }
}