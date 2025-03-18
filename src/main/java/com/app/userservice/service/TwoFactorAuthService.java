package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TwoFactorAuthService {

    @Autowired
    private UserRepository userRepository;
    
    private final GoogleAuthenticator gAuth;
    
    public TwoFactorAuthService() {
        // Cấu hình GoogleAuthenticator với thời gian dung sai 30 giây
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30))
                .setWindowSize(3) // Cho phép dung sai ±1 bước thời gian
                .build();
        
        this.gAuth = new GoogleAuthenticator(config);
    }
    
    /**
     * Tạo bí mật 2FA mới cho người dùng
     */
    @Transactional
    public Map<String, String> generateSecretKey(User user, String appName) {
        // Tạo khóa mới
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        
        // Lưu khóa vào người dùng
        user.setTwoFactorSecret(key.getKey());
        userRepository.save(user);
        
        // Tạo URL QR code
        String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                appName, user.getEmail(), key);
        
        Map<String, String> result = new HashMap<>();
        result.put("secretKey", key.getKey());
        result.put("otpAuthURL", otpAuthURL);
        
        return result;
    }
    
    /**
     * Kích hoạt 2FA cho người dùng
     */
    @Transactional
    public MessageResponse enableTwoFactor(Long userId, String verificationCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        if (user.isTwoFactorEnabled()) {
            return new MessageResponse("Xác thực hai yếu tố đã được kích hoạt", false);
        }
        
        // Kiểm tra mã OTP
        boolean isCodeValid = gAuth.authorize(user.getTwoFactorSecret(), Integer.parseInt(verificationCode));
        
        if (!isCodeValid) {
            return new MessageResponse("Mã xác thực không hợp lệ", false);
        }
        
        // Kích hoạt 2FA
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        
        return new MessageResponse("Đã kích hoạt xác thực hai yếu tố thành công", true);
    }
    
    /**
     * Vô hiệu hóa 2FA cho người dùng
     */
    @Transactional
    public MessageResponse disableTwoFactor(Long userId, String verificationCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        if (!user.isTwoFactorEnabled()) {
            return new MessageResponse("Xác thực hai yếu tố chưa được kích hoạt", false);
        }
        
        // Kiểm tra mã OTP
        boolean isCodeValid = gAuth.authorize(user.getTwoFactorSecret(), Integer.parseInt(verificationCode));
        
        if (!isCodeValid) {
            return new MessageResponse("Mã xác thực không hợp lệ", false);
        }
        
        // Vô hiệu hóa 2FA
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        
        return new MessageResponse("Đã vô hiệu hóa xác thực hai yếu tố thành công", true);
    }
    
    /**
     * Xác thực mã OTP
     */
    public boolean validateOTP(User user, String verificationCode) {
        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            throw new RuntimeException("Xác thực hai yếu tố chưa được kích hoạt");
        }
        
        return gAuth.authorize(user.getTwoFactorSecret(), Integer.parseInt(verificationCode));
    }
}