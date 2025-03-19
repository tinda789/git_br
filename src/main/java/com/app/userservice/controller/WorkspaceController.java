package com.app.userservice.controller;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.WorkspaceDTO;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WorkspaceDTO>> getAllWorkspaces() {
        List<WorkspaceDTO> workspaces = workspaceService.getAllWorkspaces();
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WorkspaceDTO>> getActiveWorkspaces() {
        List<WorkspaceDTO> workspaces = workspaceService.getActiveWorkspaces();
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WorkspaceDTO>> getWorkspacesByDepartment(@PathVariable Long departmentId) {
        List<WorkspaceDTO> workspaces = workspaceService.getWorkspacesByDepartment(departmentId);
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkspaceDTO> getWorkspaceById(@PathVariable Long id) {
        WorkspaceDTO workspace = workspaceService.getWorkspaceById(id);
        return ResponseEntity.ok(workspace);
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<MessageResponse> createWorkspace(@Valid @RequestBody WorkspaceDTO workspaceDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = workspaceService.createWorkspace(workspaceDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> updateWorkspace(
            @PathVariable Long id,
            @Valid @RequestBody WorkspaceDTO workspaceDTO) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = workspaceService.updateWorkspace(id, workspaceDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> deleteWorkspace(@PathVariable Long id) {
        MessageResponse response = workspaceService.deleteWorkspace(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}