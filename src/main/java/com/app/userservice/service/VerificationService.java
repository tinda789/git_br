package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.entity.user.User;
import com.app.userservice.entity.user.UserVerificationToken;
import com.app.userservice.repository.UserRepository;
import com.app.userservice.repository.UserVerificationTokenRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VerificationService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserVerificationTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${app.security.verification-token-expiry}")
    private long tokenExpiryMs;
    
    // Sử dụng REQUIRES_NEW để tạo giao dịch mới, tách biệt với giao dịch cha
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendVerificationEmail(User user) {
        try {
            // Xóa token cũ nếu có
            tokenRepository.deleteByUserAndTokenType(user, "EMAIL_VERIFICATION");
            
            // Tạo token mới
            String token = UUID.randomUUID().toString();
            UserVerificationToken verificationToken = new UserVerificationToken();
            verificationToken.setUser(user);
            verificationToken.setToken(token);
            verificationToken.setTokenType("EMAIL_VERIFICATION");
            verificationToken.setCreatedAt(LocalDateTime.now());
            verificationToken.setExpiresAt(LocalDateTime.now().plusSeconds(tokenExpiryMs / 1000));
            tokenRepository.save(verificationToken);
            
            // Gửi email xác thực
            try {
                emailService.sendVerificationEmail(user.getEmail(), token);
            } catch (MessagingException e) {
                System.err.println("Could not send verification email: " + e.getMessage());
                // Không ném lại ngoại lệ để tránh đánh dấu giao dịch là rollback-only
            }
        } catch (Exception e) {
            System.err.println("Error in verification process: " + e.getMessage());
            // Không ném lại ngoại lệ để tránh đánh dấu giao dịch là rollback-only
        }
    }
    
    @Transactional
    public MessageResponse verifyEmail(String token) {
        UserVerificationToken verificationToken = tokenRepository.findByTokenAndTokenType(token, "EMAIL_VERIFICATION")
                .orElse(null);
        
        if (verificationToken == null) {
            return new MessageResponse("Token không hợp lệ", false);
        }
        
        if (verificationToken.isExpired()) {
            return new MessageResponse("Token đã hết hạn", false);
        }
        
        if (verificationToken.isUsed()) {
            return new MessageResponse("Token đã được sử dụng", false);
        }
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        
        verificationToken.setUsed(true);
        verificationToken.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);
        
        return new MessageResponse("Email đã được xác thực thành công", true);
    }
    
    // Sử dụng REQUIRES_NEW để tạo giao dịch mới, tách biệt với giao dịch cha
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendPasswordResetEmail(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
            
            // Xóa token cũ nếu có
            tokenRepository.deleteByUserAndTokenType(user, "PASSWORD_RESET");
            
            // Tạo token mới
            String token = UUID.randomUUID().toString();
            UserVerificationToken resetToken = new UserVerificationToken();
            resetToken.setUser(user);
            resetToken.setToken(token);
            resetToken.setTokenType("PASSWORD_RESET");
            resetToken.setCreatedAt(LocalDateTime.now());
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(24));
            tokenRepository.save(resetToken);
            
            // Gửi email đặt lại mật khẩu
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), token);
            } catch (MessagingException e) {
                System.err.println("Could not send password reset email: " + e.getMessage());
                // Không ném lại ngoại lệ
            }
        } catch (Exception e) {
            System.err.println("Error in password reset process: " + e.getMessage());
            // Không ném lại ngoại lệ
        }
    }
    
    @Transactional
    public MessageResponse validatePasswordResetToken(String token) {
        UserVerificationToken resetToken = tokenRepository.findByTokenAndTokenType(token, "PASSWORD_RESET")
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
        
        return new MessageResponse("Token hợp lệ", true);
    }
}