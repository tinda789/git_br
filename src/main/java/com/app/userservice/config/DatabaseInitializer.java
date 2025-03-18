package com.app.userservice.config;

import com.app.userservice.entity.user.Permission;
import com.app.userservice.entity.user.Role;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.PermissionRepository;
import com.app.userservice.repository.RoleRepository;
import com.app.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

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
        // Initialize default permissions
        createPermissionsIfNotFound();
        
        // Initialize default roles with permissions
        createRolesIfNotFound();
        
        // Create admin user
        createAdminIfNotFound();
        
        // Create system admin user
        createSystemAdminIfNotFound();
    }

    private void createPermissionsIfNotFound() {
        // User permissions
        createPermissionIfNotFound("USER_READ", "Read user information", "user", "read");
        createPermissionIfNotFound("USER_CREATE", "Create users", "user", "create");
        createPermissionIfNotFound("USER_UPDATE", "Update users", "user", "update");
        createPermissionIfNotFound("USER_DELETE", "Delete users", "user", "delete");
        
        // Project permissions
        createPermissionIfNotFound("PROJECT_READ", "Read project information", "project", "read");
        createPermissionIfNotFound("PROJECT_CREATE", "Create projects", "project", "create");
        createPermissionIfNotFound("PROJECT_UPDATE", "Update projects", "project", "update");
        createPermissionIfNotFound("PROJECT_DELETE", "Delete projects", "project", "delete");
        
 // Workspace permissions
        createPermissionIfNotFound("WORKSPACE_READ", "Read workspace information", "workspace", "read");
        createPermissionIfNotFound("WORKSPACE_CREATE", "Create workspaces", "workspace", "create");
        createPermissionIfNotFound("WORKSPACE_UPDATE", "Update workspaces", "workspace", "update");
        createPermissionIfNotFound("WORKSPACE_DELETE", "Delete workspaces", "workspace", "delete");

        // Task permissions
        createPermissionIfNotFound("TASK_READ", "Read task information", "task", "read");
        createPermissionIfNotFound("TASK_CREATE", "Create tasks", "task", "create");
        createPermissionIfNotFound("TASK_UPDATE", "Update tasks", "task", "update");
        createPermissionIfNotFound("TASK_DELETE", "Delete tasks", "task", "delete");
        createPermissionIfNotFound("TASK_ASSIGN", "Assign tasks to others", "task", "assign");
        createPermissionIfNotFound("TASK_COMMENT", "Comment on tasks", "task", "comment");

        // Document permissions
        createPermissionIfNotFound("DOCUMENT_READ", "Read documents", "document", "read");
        createPermissionIfNotFound("DOCUMENT_CREATE", "Create documents", "document", "create");
        createPermissionIfNotFound("DOCUMENT_UPDATE", "Update documents", "document", "update");
        createPermissionIfNotFound("DOCUMENT_DELETE", "Delete documents", "document", "delete");
        
      
    }

    private Permission createPermissionIfNotFound(String name, String description, String resourceName, String actionName) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setDescription(description);
                    permission.setResourceName(resourceName);
                    permission.setActionName(actionName);
                    permission.setPermissionType(actionName.toUpperCase());
                    permission.setSystem(true);
                    return permissionRepository.save(permission);
                });
    }

    private void createRolesIfNotFound() {
        // Create USER role
        Set<Permission> userPermissions = new HashSet<>(Arrays.asList(
                permissionRepository.findByName("USER_READ").orElseThrow(),
                permissionRepository.findByName("TASK_READ").orElseThrow(),
                permissionRepository.findByName("PROJECT_READ").orElseThrow(),
                permissionRepository.findByName("DOCUMENT_READ").orElseThrow()
        ));
        createRoleIfNotFound("USER", "Basic authenticated user", userPermissions);
        
        // Create EMPLOYEE role
        Set<Permission> employeePermissions = new HashSet<>(Arrays.asList(
                permissionRepository.findByName("USER_READ").orElseThrow(),
                permissionRepository.findByName("TASK_READ").orElseThrow(),
                permissionRepository.findByName("TASK_CREATE").orElseThrow(),
                permissionRepository.findByName("TASK_UPDATE").orElseThrow(),
                permissionRepository.findByName("PROJECT_READ").orElseThrow(),
                permissionRepository.findByName("DOCUMENT_READ").orElseThrow(),
                permissionRepository.findByName("DOCUMENT_CREATE").orElseThrow(),
                permissionRepository.findByName("DOCUMENT_UPDATE").orElseThrow()
        ));
        createRoleIfNotFound("EMPLOYEE", "Company employee", employeePermissions);
        
        // Create MANAGER role
        Set<Permission> managerPermissions = new HashSet<>(Arrays.asList(
                permissionRepository.findByName("USER_READ").orElseThrow(),
                permissionRepository.findByName("TASK_READ").orElseThrow(),
                permissionRepository.findByName("TASK_CREATE").orElseThrow(),
                permissionRepository.findByName("TASK_UPDATE").orElseThrow(),
                permissionRepository.findByName("TASK_DELETE").orElseThrow(),
                permissionRepository.findByName("PROJECT_READ").orElseThrow(),
                permissionRepository.findByName("PROJECT_CREATE").orElseThrow(),
                permissionRepository.findByName("PROJECT_UPDATE").orElseThrow(),
                permissionRepository.findByName("DOCUMENT_READ").orElseThrow(),
                permissionRepository.findByName("DOCUMENT_CREATE").orElseThrow(),
                permissionRepository.findByName("DOCUMENT_UPDATE").orElseThrow(),
                permissionRepository.findByName("DOCUMENT_DELETE").orElseThrow()
        ));
        createRoleIfNotFound("MANAGER", "Department manager", managerPermissions);
        
        // Create ADMIN role
        Set<Permission> adminPermissions = permissionRepository.findAll()
                .stream().collect(java.util.stream.Collectors.toSet());
        // Remove tenant permissions from regular admin
        adminPermissions.removeIf(p -> p.getResourceName() != null && p.getResourceName().equals("tenant"));
        createRoleIfNotFound("ADMIN", "Company administrator", adminPermissions);
        
        // Create SYSTEM_ADMIN role
        Set<Permission> systemAdminPermissions = permissionRepository.findAll()
                .stream().collect(java.util.stream.Collectors.toSet());
        createRoleIfNotFound("SYSTEM_ADMIN", "System administrator with full access", systemAdminPermissions);
    }

    private Role createRoleIfNotFound(String name, String description, Set<Permission> permissions) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setPermissions(permissions);
                    role.setSystem(true);
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