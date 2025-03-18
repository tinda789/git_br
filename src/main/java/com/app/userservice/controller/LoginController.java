package com.app.userservice.controller;

import com.app.userservice.entity.user.LoginHistory;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.UserRepository;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.service.LoginHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/login-history")
public class LoginController {

    @Autowired
    private LoginHistoryService loginHistoryService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/recent")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getRecentLoginHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        List<LoginHistory> loginHistories = loginHistoryService.getRecentLoginHistory(user);
        
        return ResponseEntity.ok(loginHistories);
    }
}