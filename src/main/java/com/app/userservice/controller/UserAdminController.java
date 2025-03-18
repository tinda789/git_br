package com.app.userservice.controller;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.UserAdminDTO;
import com.app.userservice.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    @Autowired
    private UserAdminService userAdminService;
    
    @GetMapping
    public ResponseEntity<List<UserAdminDTO>> getAllUsers() {
        List<UserAdminDTO> users = userAdminService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserAdminDTO> getUserById(@PathVariable Long id) {
        UserAdminDTO user = userAdminService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    public ResponseEntity<MessageResponse> createUser(@Valid @RequestBody UserAdminDTO userDTO) {
        MessageResponse response = userAdminService.createUser(userDTO);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserAdminDTO userDTO) {
        MessageResponse response = userAdminService.updateUser(id, userDTO);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        MessageResponse response = userAdminService.deleteUser(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<MessageResponse> updateUserStatus(@PathVariable Long id, @RequestParam int status) {
        MessageResponse response = userAdminService.updateUserStatus(id, status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}/roles")
    public ResponseEntity<MessageResponse> updateUserRoles(@PathVariable Long id, @RequestBody List<String> roleNames) {
        MessageResponse response = userAdminService.updateUserRoles(id, roleNames);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}/lock")
    public ResponseEntity<MessageResponse> lockUser(@PathVariable Long id) {
        MessageResponse response = userAdminService.lockUser(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}/unlock")
    public ResponseEntity<MessageResponse> unlockUser(@PathVariable Long id) {
        MessageResponse response = userAdminService.unlockUser(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = userAdminService.getUserStats();
        return ResponseEntity.ok(stats);
    }
}