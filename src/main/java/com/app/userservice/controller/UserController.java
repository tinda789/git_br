package com.app.userservice.controller;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.UpdatePasswordRequest;
import com.app.userservice.dto.UpdateProfileRequest;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/info")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getCurrentUserInfo() {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Get user info from service
        Map<String, Object> userInfo = userService.getUserInfo(userDetails.getId());
        
        // Add authorities from current authentication
        userInfo.put("roles", authentication.getAuthorities());
        
        return ResponseEntity.ok(userInfo);
    }
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest profileRequest) {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Update profile using service
        MessageResponse response = userService.updateProfile(userDetails.getId(), profileRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/password")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody UpdatePasswordRequest passwordRequest) {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Change password using service
        MessageResponse response = userService.changePassword(userDetails.getId(), passwordRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/employee")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<?> employeeAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Employee access granted");
        response.put("username", userDetails.getUsername());
        response.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> managerAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Manager access granted");
        response.put("username", userDetails.getUsername());
        response.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<?> adminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin access granted");
        response.put("username", userDetails.getUsername());
        response.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(response);
    }
}