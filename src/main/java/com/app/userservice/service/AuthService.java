package com.app.userservice.service;

import com.app.userservice.dto.JwtResponse;
import com.app.userservice.dto.LoginRequest;
import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.SignupRequest;
import com.app.userservice.entity.tenant.Tenant;
import com.app.userservice.entity.user.LoginHistory;
import com.app.userservice.entity.user.Role;
import com.app.userservice.entity.user.User;
import com.app.userservice.entity.user.UserPreferences;
import com.app.userservice.repository.LoginHistoryRepository;
import com.app.userservice.repository.RoleRepository;
import com.app.userservice.repository.TenantRepository;
import com.app.userservice.repository.UserRepository;
import com.app.userservice.security.jwt.JwtUtils;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private LoginHistoryRepository loginHistoryRepository;
    
    @Autowired
    private PasswordEncoder encoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Value("${app.default-schema:public}")
    private String defaultSchema;
    
    /**
     * Authenticate user and generate JWT token
     */
    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request) {
        // Reset to default schema for authentication
        TenantContext.setCurrentTenant(defaultSchema);
        
        // Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // Get user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Get user roles
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        // Save login history
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        
        // Get tenant schema (if exists)
        String schema = defaultSchema;
        if (user.getTenant() != null) {
            schema = user.getTenant().getSchema();
            // Set tenant context for subsequent operations
            TenantContext.setCurrentTenant(schema);
        }
        
        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Create login history record
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setIpAddress(getClientIp(request));
        loginHistory.setUserAgent(request.getHeader("User-Agent"));
        loginHistory.setLoginStatus(true);
        loginHistory.setLoginMethod("PASSWORD");
        loginHistoryRepository.save(loginHistory);
        
        // Add tenant info to response
        JwtResponse response = new JwtResponse(
                jwt, 
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFullName(),
                roles);
                
        // Reset tenant context
        TenantContext.setCurrentTenant(defaultSchema);
        
        return response;
    }
    
    /**
     * Register a new user with default roles USER and EMPLOYEE
     */
    @Transactional
    public MessageResponse registerUser(SignupRequest signupRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!", false);
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!", false);
        }
        
        // Create new user
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(encoder.encode(signupRequest.getPassword()));
        user.setFullName(signupRequest.getFullName());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setStatus(1); // Active
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        
        // Assign default roles: USER and EMPLOYEE
        Set<Role> roles = new HashSet<>();
        
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
        roles.add(userRole);
        
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Error: Role EMPLOYEE is not found."));
        roles.add(employeeRole);
        
        user.setRoles(roles);
        
        // Save user
        userRepository.save(user);
        
        // Create user preferences with defaults
        UserPreferences preferences = new UserPreferences();
        preferences.setUser(user);
        // Default preferences are set by the entity default values
        
        return new MessageResponse("User registered successfully!", true);
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || remoteAddr.isEmpty()) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        
        return remoteAddr;
    }
}