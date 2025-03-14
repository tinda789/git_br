package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.UpdatePasswordRequest;
import com.app.userservice.dto.UpdateProfileRequest;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Get current user information
     */
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("phoneNumber", user.getPhoneNumber());
        userInfo.put("profileImage", user.getProfileImage());
        userInfo.put("status", user.getStatus());
        userInfo.put("emailVerified", user.isEmailVerified());
        userInfo.put("lastLoginAt", user.getLastLoginAt());
        
        return userInfo;
    }
    
    /**
     * Update user profile
     */
    @Transactional
    public MessageResponse updateProfile(Long userId, UpdateProfileRequest profileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if email is being changed and is already taken
        if (!user.getEmail().equals(profileRequest.getEmail()) && 
            userRepository.existsByEmail(profileRequest.getEmail())) {
            return new MessageResponse("Email is already in use!", false);
        }
        
        // Update user profile
        user.setEmail(profileRequest.getEmail());
        user.setFullName(profileRequest.getFullName());
        user.setPhoneNumber(profileRequest.getPhoneNumber());
        if (profileRequest.getProfileImage() != null && !profileRequest.getProfileImage().isEmpty()) {
            user.setProfileImage(profileRequest.getProfileImage());
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save updated user
        userRepository.save(user);
        
        return new MessageResponse("Profile updated successfully!", true);
    }
    
    /**
     * Change user password
     */
    @Transactional
    public MessageResponse changePassword(Long userId, UpdatePasswordRequest passwordRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), user.getPassword())) {
            return new MessageResponse("Current password is incorrect!", false);
        }
        
        // Verify that new password and confirm password match
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
            return new MessageResponse("New password and confirm password do not match!", false);
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save updated user
        userRepository.save(user);
        
        return new MessageResponse("Password updated successfully!", true);
    }
}