package com.app.userservice.controller;

import com.app.userservice.dto.EmployeeDTO;
import com.app.userservice.dto.MessageResponse;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByCompany(@PathVariable Long companyId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByCompany(companyId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByTeam(@PathVariable Long teamId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByTeam(teamId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/position/{positionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByPosition(@PathVariable Long positionId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByPosition(positionId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByManager(@PathVariable Long managerId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EmployeeDTO> getEmployeeByUserId(@PathVariable Long userId) {
        EmployeeDTO employee = employeeService.getEmployeeByUserId(userId);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCurrentEmployeeInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            EmployeeDTO employee = employeeService.getEmployeeByUserId(userDetails.getId());
            return ResponseEntity.ok(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new MessageResponse("No employee record found for current user", false));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<MessageResponse> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = employeeService.createEmployee(employeeDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<MessageResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = employeeService.updateEmployee(id, employeeDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<MessageResponse> deactivateEmployee(@PathVariable Long id) {
        MessageResponse response = employeeService.deactivateEmployee(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/terminate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<MessageResponse> terminateEmployee(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime terminationDate) {
        
        MessageResponse response = employeeService.terminateEmployee(id, terminationDate);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}