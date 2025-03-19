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
        
        // Company permissions
        createPermissionIfNotFound("COMPANY_READ", "Xem thông tin công ty", "company", "read");
        createPermissionIfNotFound("COMPANY_CREATE", "Tạo công ty mới", "company", "create");
        createPermissionIfNotFound("COMPANY_UPDATE", "Cập nhật thông tin công ty", "company", "update");
        createPermissionIfNotFound("COMPANY_DELETE", "Xóa công ty", "company", "delete");
        
        // Department permissions
        createPermissionIfNotFound("DEPARTMENT_READ", "Xem thông tin phòng ban", "department", "read");
        createPermissionIfNotFound("DEPARTMENT_CREATE", "Tạo phòng ban mới", "department", "create");
        createPermissionIfNotFound("DEPARTMENT_UPDATE", "Cập nhật thông tin phòng ban", "department", "update");
        createPermissionIfNotFound("DEPARTMENT_DELETE", "Xóa phòng ban", "department", "delete");
        
        // Team permissions
        createPermissionIfNotFound("TEAM_READ", "Xem thông tin nhóm", "team", "read");
        createPermissionIfNotFound("TEAM_CREATE", "Tạo nhóm mới", "team", "create");
        createPermissionIfNotFound("TEAM_UPDATE", "Cập nhật thông tin nhóm", "team", "update");
        createPermissionIfNotFound("TEAM_DELETE", "Xóa nhóm", "team", "delete");
        
        // Position permissions
        createPermissionIfNotFound("POSITION_READ", "Xem thông tin vị trí", "position", "read");
        createPermissionIfNotFound("POSITION_CREATE", "Tạo vị trí mới", "position", "create");
        createPermissionIfNotFound("POSITION_UPDATE", "Cập nhật thông tin vị trí", "position", "update");
        createPermissionIfNotFound("POSITION_DELETE", "Xóa vị trí", "position", "delete");
        
        // Employee permissions
        createPermissionIfNotFound("EMPLOYEE_READ", "Xem thông tin nhân viên", "employee", "read");
        createPermissionIfNotFound("EMPLOYEE_CREATE", "Tạo hồ sơ nhân viên mới", "employee", "create");
        createPermissionIfNotFound("EMPLOYEE_UPDATE", "Cập nhật thông tin nhân viên", "employee", "update");
        createPermissionIfNotFound("EMPLOYEE_DELETE", "Vô hiệu hóa nhân viên", "employee", "delete");
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
                permissionRepository.findByName("DOCUMENT_READ").orElseThrow(),
                permissionRepository.findByName("COMPANY_READ").orElseThrow(),
                permissionRepository.findByName("DEPARTMENT_READ").orElseThrow(),
                permissionRepository.findByName("TEAM_READ").orElseThrow(),
                permissionRepository.findByName("POSITION_READ").orElseThrow(),
                permissionRepository.findByName("EMPLOYEE_READ").orElseThrow()
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
                permissionRepository.findByName("DOCUMENT_UPDATE").orElseThrow(),
                permissionRepository.findByName("COMPANY_READ").orElseThrow(),
                permissionRepository.findByName("DEPARTMENT_READ").orElseThrow(),
                permissionRepository.findByName("TEAM_READ").orElseThrow(),
                permissionRepository.findByName("POSITION_READ").orElseThrow(),
                permissionRepository.findByName("EMPLOYEE_READ").orElseThrow()
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
                permissionRepository.findByName("DOCUMENT_DELETE").orElseThrow(),
                permissionRepository.findByName("COMPANY_READ").orElseThrow(),
                permissionRepository.findByName("DEPARTMENT_READ").orElseThrow(),
                permissionRepository.findByName("DEPARTMENT_UPDATE").orElseThrow(),
                permissionRepository.findByName("TEAM_READ").orElseThrow(),
                permissionRepository.findByName("TEAM_CREATE").orElseThrow(),
                permissionRepository.findByName("TEAM_UPDATE").orElseThrow(),
                permissionRepository.findByName("POSITION_READ").orElseThrow(),
                permissionRepository.findByName("POSITION_CREATE").orElseThrow(),
                permissionRepository.findByName("POSITION_UPDATE").orElseThrow(),
                permissionRepository.findByName("EMPLOYEE_READ").orElseThrow(),
                permissionRepository.findByName("EMPLOYEE_UPDATE").orElseThrow()
        ));
        createRoleIfNotFound("MANAGER", "Department manager", managerPermissions);

        // Create HR role
Set<Permission> hrPermissions = new HashSet<>(Arrays.asList(
    permissionRepository.findByName("USER_READ").orElseThrow(),
    permissionRepository.findByName("TASK_READ").orElseThrow(),
    permissionRepository.findByName("TASK_CREATE").orElseThrow(),
    permissionRepository.findByName("TASK_UPDATE").orElseThrow(),
    permissionRepository.findByName("PROJECT_READ").orElseThrow(),
    permissionRepository.findByName("DOCUMENT_READ").orElseThrow(),
    permissionRepository.findByName("DOCUMENT_CREATE").orElseThrow(),
    permissionRepository.findByName("DOCUMENT_UPDATE").orElseThrow(),
    permissionRepository.findByName("COMPANY_READ").orElseThrow(),
    permissionRepository.findByName("DEPARTMENT_READ").orElseThrow(),
    permissionRepository.findByName("TEAM_READ").orElseThrow(),
    permissionRepository.findByName("POSITION_READ").orElseThrow(),
    permissionRepository.findByName("POSITION_CREATE").orElseThrow(),
    permissionRepository.findByName("POSITION_UPDATE").orElseThrow(),
    permissionRepository.findByName("EMPLOYEE_READ").orElseThrow(),
    permissionRepository.findByName("EMPLOYEE_CREATE").orElseThrow(),
    permissionRepository.findByName("EMPLOYEE_UPDATE").orElseThrow(),
    permissionRepository.findByName("EMPLOYEE_DELETE").orElseThrow()
));
createRoleIfNotFound("HR", "Human Resources manager", hrPermissions);
        
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