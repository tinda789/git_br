package com.app.userservice.controller;

import com.app.userservice.dto.DepartmentDTO;
import com.app.userservice.dto.MessageResponse;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.service.DepartmentService;
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
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsByCompany(@PathVariable Long companyId) {
        List<DepartmentDTO> departments = departmentService.getDepartmentsByCompany(companyId);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/root")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DepartmentDTO>> getRootDepartments() {
        List<DepartmentDTO> departments = departmentService.getRootDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/parent/{parentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DepartmentDTO>> getSubDepartments(@PathVariable Long parentId) {
        List<DepartmentDTO> departments = departmentService.getSubDepartments(parentId);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER','SYSTEM_ADMIN')")
    public ResponseEntity<MessageResponse> createDepartment(@Valid @RequestBody DepartmentDTO departmentDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = departmentService.createDepartment(departmentDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDTO departmentDTO) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = departmentService.updateDepartment(id, departmentDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> deleteDepartment(@PathVariable Long id) {
        MessageResponse response = departmentService.deleteDepartment(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}