package com.app.userservice.service;

import com.app.userservice.entity.user.LoginHistory;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.LoginHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoginHistoryService {

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;
    
    /**
     * Ghi nhật ký đăng nhập thành công
     */
    @Transactional
    public void recordSuccessfulLogin(User user, HttpServletRequest request, String loginMethod) {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setIpAddress(getClientIp(request));
        loginHistory.setUserAgent(request.getHeader("User-Agent"));
        loginHistory.setDeviceInfo(extractDeviceInfo(request.getHeader("User-Agent")));
        loginHistory.setLoginStatus(true);
        loginHistory.setLoginMethod(loginMethod);
        loginHistory.setLoginTime(LocalDateTime.now());
        
        loginHistoryRepository.save(loginHistory);
    }
    
    /**
     * Ghi nhật ký đăng nhập thất bại
     */
    @Transactional
    public void recordFailedLogin(User user, HttpServletRequest request, String loginMethod, String failureReason) {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setIpAddress(getClientIp(request));
        loginHistory.setUserAgent(request.getHeader("User-Agent"));
        loginHistory.setDeviceInfo(extractDeviceInfo(request.getHeader("User-Agent")));
        loginHistory.setLoginStatus(false);
        loginHistory.setLoginMethod(loginMethod);
        loginHistory.setLoginTime(LocalDateTime.now());
        loginHistory.setFailureReason(failureReason);
        
        loginHistoryRepository.save(loginHistory);
    }
    
    /**
     * Ghi nhật ký đăng xuất
     */
    @Transactional
    public void recordLogout(User user, LocalDateTime loginTime) {
        List<LoginHistory> loginHistories = loginHistoryRepository.findByUserOrderByLoginTimeDesc(user);
        
        if (!loginHistories.isEmpty()) {
            LoginHistory lastLogin = loginHistories.get(0);
            lastLogin.setLogoutTime(LocalDateTime.now());
            lastLogin.setSessionDuration(
                    java.time.Duration.between(lastLogin.getLoginTime(), lastLogin.getLogoutTime()).getSeconds());
            loginHistoryRepository.save(lastLogin);
        }
    }
    
    /**
     * Lấy 10 lịch sử đăng nhập gần nhất của người dùng
     */
    public List<LoginHistory> getRecentLoginHistory(User user) {
        return loginHistoryRepository.findTop10ByUserOrderByLoginTimeDesc(user);
    }
    
    /**
     * Lấy địa chỉ IP của client
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    /**
     * Trích xuất thông tin thiết bị từ User-Agent
     */
    private String extractDeviceInfo(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        
        // Đơn giản hóa: trích xuất thông tin cơ bản từ User-Agent
        if (userAgent.contains("Mobile")) {
            return "Mobile";
        } else if (userAgent.contains("Tablet")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
}