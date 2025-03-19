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
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER', 'HR') or hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER', 'HR') or hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByCompany(@PathVariable Long companyId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByCompany(companyId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER', 'HR') or hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/secondary-department/{departmentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER', 'HR') or hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesBySecondaryDepartment(@PathVariable Long departmentId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesBySecondaryDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/any-department/{departmentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER', 'HR') or hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByAnyDepartment(@PathVariable Long departmentId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByAnyDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER', 'HR') or hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByTeam(@PathVariable Long teamId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByTeam(teamId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/position/{positionId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER', 'HR') or hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByPosition(@PathVariable Long positionId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByPosition(positionId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER', 'HR') or hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByManager(@PathVariable Long managerId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER') or hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER') or hasAuthority('EMPLOYEE_READ')")
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
            return ResponseEntity.ok(new MessageResponse("Không tìm thấy hồ sơ nhân viên cho người dùng hiện tại", false));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'HR') or hasAuthority('EMPLOYEE_CREATE')")
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
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'HR') or hasAuthority('EMPLOYEE_UPDATE')")
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
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'HR') or hasAuthority('EMPLOYEE_DELETE')")
    public ResponseEntity<MessageResponse> deactivateEmployee(@PathVariable Long id) {
        MessageResponse response = employeeService.deactivateEmployee(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/terminate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'HR') or hasAuthority('EMPLOYEE_DELETE')")
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