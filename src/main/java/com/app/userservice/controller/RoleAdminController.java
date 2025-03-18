package com.app.userservice.controller;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.RoleDTO;
import com.app.userservice.service.RoleAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleAdminController {

    @Autowired
    private RoleAdminService roleAdminService;
    
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleAdminService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        RoleDTO role = roleAdminService.getRoleById(id);
        return ResponseEntity.ok(role);
    }
    
    @PostMapping
    public ResponseEntity<MessageResponse> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        MessageResponse response = roleAdminService.createRole(roleDTO);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        MessageResponse response = roleAdminService.updateRole(id, roleDTO);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteRole(@PathVariable Long id) {
        MessageResponse response = roleAdminService.deleteRole(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}/permissions")
    public ResponseEntity<MessageResponse> updateRolePermissions(
            @PathVariable Long id, @RequestBody List<String> permissionNames) {
        MessageResponse response = roleAdminService.updateRolePermissions(id, permissionNames);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}