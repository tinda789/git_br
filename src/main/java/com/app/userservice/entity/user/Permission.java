package com.app.userservice.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "resource_name", length = 50)
    private String resourceName;

    @Column(name = "action_name", length = 30)
    private String actionName;
    
    // CREATE, READ, UPDATE, DELETE, EXECUTE, etc.
    @Column(name = "permission_type", length = 20)
    private String permissionType;
    
    // For system permissions that can't be modified
    @Column(name = "is_system")
    private boolean isSystem = false;
}