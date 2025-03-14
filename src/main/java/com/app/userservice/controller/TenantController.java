package com.app.userservice.controller;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.TenantDTO;
import com.app.userservice.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<TenantDTO>> getAllTenants() {
        List<TenantDTO> tenants = tenantService.getAllTenants();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<TenantDTO> getTenantById(@PathVariable Long id) {
        TenantDTO tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(tenant);
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<MessageResponse> createTenant(@Valid @RequestBody TenantDTO tenantDTO) {
        MessageResponse response = tenantService.createTenant(tenantDTO);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<MessageResponse> activateTenant(@PathVariable Long id) {
        MessageResponse response = tenantService.setTenantActiveStatus(id, true);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<MessageResponse> deactivateTenant(@PathVariable Long id) {
        MessageResponse response = tenantService.setTenantActiveStatus(id, false);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}