package com.app.userservice.config;

import com.app.userservice.entity.user.Role;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.RoleRepository;
import com.app.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;
    
    @Value("${app.system-admin.username:sysadmin}")
    private String systemAdminUsername;
    
    @Value("${app.system-admin.email:sysadmin@example.com}")
    private String systemAdminEmail;
    
    @Value("${app.system-admin.password:sysadmin123}")
    private String systemAdminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default roles
        createRolesIfNotFound();
        
        // Create admin user
        createAdminIfNotFound();
        
        // Create system admin user
        createSystemAdminIfNotFound();
    }

    private void createRolesIfNotFound() {
        // Create USER role
        createRoleIfNotFound("USER", "Basic authenticated user");
        
        // Create EMPLOYEE role
        createRoleIfNotFound("EMPLOYEE", "Company employee");
        
        // Create MANAGER role
        createRoleIfNotFound("MANAGER", "Department manager");

        // Create HR role
        createRoleIfNotFound("HR", "Human Resources manager");
        
        // Create ADMIN role
        createRoleIfNotFound("ADMIN", "Company administrator");
        
        // Create SYSTEM_ADMIN role
        createRoleIfNotFound("SYSTEM_ADMIN", "System administrator with full access");
    }

    private Role createRoleIfNotFound(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setSystem(name.equals("SYSTEM_ADMIN") || name.equals("ADMIN"));
                    return roleRepository.save(role);
                });
    }

    private void createAdminIfNotFound() {
        userRepository.findByUsername(adminUsername)
                .orElseGet(() -> {
                    User admin = new User();
                    admin.setUsername(adminUsername);
                    admin.setEmail(adminEmail);
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setFullName("Company Administrator");
                    admin.setStatus(1);
                    admin.setEmailVerified(true);
                    admin.setCreatedAt(LocalDateTime.now());
                    
                    Set<Role> roles = new HashSet<>();
                    roles.add(roleRepository.findByName("ADMIN").orElseThrow());
                    roles.add(roleRepository.findByName("USER").orElseThrow());
                    roles.add(roleRepository.findByName("EMPLOYEE").orElseThrow());
                    roles.add(roleRepository.findByName("MANAGER").orElseThrow());

                    admin.setRoles(roles);
                    
                    return userRepository.save(admin);
                });
    }
    
    private void createSystemAdminIfNotFound() {
        userRepository.findByUsername(systemAdminUsername)
                .orElseGet(() -> {
                    User sysAdmin = new User();
                    sysAdmin.setUsername(systemAdminUsername);
                    sysAdmin.setEmail(systemAdminEmail);
                    sysAdmin.setPassword(passwordEncoder.encode(systemAdminPassword));
                    sysAdmin.setFullName("System Administrator");
                    sysAdmin.setStatus(1);
                    sysAdmin.setEmailVerified(true);
                    sysAdmin.setCreatedAt(LocalDateTime.now());
                    
                    Set<Role> roles = new HashSet<>();
                    roles.add(roleRepository.findByName("SYSTEM_ADMIN").orElseThrow());
                    roles.add(roleRepository.findByName("USER").orElseThrow());
                    sysAdmin.setRoles(roles);
                    
                    return userRepository.save(sysAdmin);
                });
    }
}