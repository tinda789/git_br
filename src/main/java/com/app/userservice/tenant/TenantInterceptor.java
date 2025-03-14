package com.app.userservice.tenant;

import com.app.userservice.entity.tenant.Tenant;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.TenantRepository;
import com.app.userservice.repository.UserRepository;
import com.app.userservice.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Value("${app.default-schema:public}")
    private String defaultSchema;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Default to public schema
        String schema = defaultSchema;
        
        // URLs that bypass tenant resolution (public endpoints, auth, etc.)
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth") || requestURI.startsWith("/api/public")) {
            TenantContext.setCurrentTenant(schema);
            System.out.println("Setting default tenant for auth/public endpoint: " + schema);
            return true;
        }
        
        // Extract JWT token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            if (jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                System.out.println("Extracted username from JWT: " + username);
                
                // Find user and their tenant
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null && user.getTenant() != null) {
                    Tenant tenant = user.getTenant();
                    schema = tenant.getSchema();
                    System.out.println("Setting tenant context to: " + schema + " for user: " + username);
                } else {
                    System.out.println("User or tenant is null for username: " + username);
                }
            } else {
                System.out.println("Invalid JWT token");
            }
        } else {
            System.out.println("No Authorization header or not Bearer token");
        }
        
        // Set current tenant context
        TenantContext.setCurrentTenant(schema);
        System.out.println("Current tenant at preHandle: " + TenantContext.getCurrentTenant());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Log the current tenant before clearing
        System.out.println("Current tenant at postHandle: " + TenantContext.getCurrentTenant());
        
        // DO NOT clear the tenant context here as it might be needed until the transaction is complete
        // TenantContext.clear();
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Clear the tenant context after the request is fully completed (including transaction)
        System.out.println("Clearing tenant context. Current value: " + TenantContext.getCurrentTenant());
        TenantContext.clear();
    }
}