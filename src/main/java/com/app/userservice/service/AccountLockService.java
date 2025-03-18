package com.app.userservice.service;

import com.app.userservice.entity.user.User;
import com.app.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountLockService {

    @Autowired
    private UserRepository userRepository;
    
    @Value("${app.security.max-failed-attempts}")
    private int maxFailedAttempts;
    
    @Value("${app.security.lock-time-duration}")
    private long lockTimeDuration;
    
    /**
     * Tăng số lần đăng nhập thất bại và khóa tài khoản nếu vượt quá giới hạn
     */
    @Transactional
    public void increaseFailedAttempts(User user) {
        int newFailedAttempts = user.getFailedAttempt() + 1;
        userRepository.updateFailedAttempts(newFailedAttempts, user.getUsername());
        
        if (newFailedAttempts >= maxFailedAttempts) {
            lockUser(user);
        }
    }
    
    /**
     * Đặt lại số lần đăng nhập thất bại về 0
     */
    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.updateFailedAttempts(0, username);
    }
    
    /**
     * Khóa tài khoản
     */
    @Transactional
    public void lockUser(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now());
        
        userRepository.save(user);
    }
    
    /**
     * Mở khóa tài khoản đã hết hạn khóa
     */
    @Transactional
    public boolean unlockWhenTimeExpired(User user) {
        if (user.getLockTime() != null) {
            LocalDateTime lockTimeInMs = user.getLockTime();
            LocalDateTime unlockTimeInMs = lockTimeInMs.plusSeconds(lockTimeDuration / 1000);
            
            if (LocalDateTime.now().isAfter(unlockTimeInMs)) {
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                user.setFailedAttempt(0);
                
                userRepository.save(user);
                
                return true;
            }
        }
        
        return false;
    }
}