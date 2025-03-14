package com.app.userservice.service;

import com.app.userservice.dto.DepartmentDTO;
import com.app.userservice.dto.MessageResponse;
import com.app.userservice.entity.organization.Company;
import com.app.userservice.entity.organization.Department;
import com.app.userservice.repository.CompanyRepository;
import com.app.userservice.repository.DepartmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Get all departments
     */
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get departments by company ID
     */
    public List<DepartmentDTO> getDepartmentsByCompany(Long companyId) {
        return departmentRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get department by ID
     */
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return convertToDTO(department);
    }

    /**
     * Get root departments (without parent)
     */
    public List<DepartmentDTO> getRootDepartments() {
        return departmentRepository.findByParentIsNull().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get subdepartments by parent ID
     */
    public List<DepartmentDTO> getSubDepartments(Long parentId) {
        return departmentRepository.findByParentId(parentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new department
     */
    @Transactional
    public MessageResponse createDepartment(DepartmentDTO departmentDTO, Long userId) {
        // Check if company exists
        Company company = companyRepository.findById(departmentDTO.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Check if department name exists in the company
        if (departmentRepository.existsByNameAndCompanyId(
                departmentDTO.getName(), departmentDTO.getCompanyId())) {
            return new MessageResponse("Department name already exists in this company", false);
        }

        // Check if department code exists in the company
        if (departmentDTO.getCode() != null && !departmentDTO.getCode().isEmpty() &&
                departmentRepository.existsByCodeAndCompanyId(
                        departmentDTO.getCode(), departmentDTO.getCompanyId())) {
            return new MessageResponse("Department code already exists in this company", false);
        }

        // Create new department
        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setCode(departmentDTO.getCode());
        department.setCompany(company);
        
        // Set parent department if provided
        if (departmentDTO.getParentId() != null) {
            Department parent = departmentRepository.findById(departmentDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent department not found"));
            department.setParent(parent);
        }
        
        department.setCreatedAt(LocalDateTime.now());
        department.setCreatedBy(userId);

        departmentRepository.save(department);

        return new MessageResponse("Department created successfully", true);
    }

    /**
     * Update an existing department
     */
    @Transactional
    public MessageResponse updateDepartment(Long id, DepartmentDTO departmentDTO, Long userId) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Check if company exists
        Company company = companyRepository.findById(departmentDTO.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Check if department name exists in the company
        if (!department.getName().equals(departmentDTO.getName()) &&
                departmentRepository.existsByNameAndCompanyId(
                        departmentDTO.getName(), departmentDTO.getCompanyId())) {
            return new MessageResponse("Department name already exists in this company", false);
        }

        // Check if department code exists in the company
        if (departmentDTO.getCode() != null && !departmentDTO.getCode().isEmpty() &&
                !departmentDTO.getCode().equals(department.getCode()) &&
                departmentRepository.existsByCodeAndCompanyId(
                        departmentDTO.getCode(), departmentDTO.getCompanyId())) {
            return new MessageResponse("Department code already exists in this company", false);
        }

        // Prevent circular reference in parent-child relationship
        if (departmentDTO.getParentId() != null && departmentDTO.getParentId().equals(id)) {
            return new MessageResponse("Department cannot be its own parent", false);
        }

        // Update department
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setCode(departmentDTO.getCode());
        department.setCompany(company);
        
        // Set parent department if provided
        if (departmentDTO.getParentId() != null) {
            Department parent = departmentRepository.findById(departmentDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent department not found"));
            department.setParent(parent);
        } else {
            department.setParent(null);
        }
        
        department.setUpdatedAt(LocalDateTime.now());
        department.setUpdatedBy(userId);

        departmentRepository.save(department);

        return new MessageResponse("Department updated successfully", true);
    }

    /**
     * Delete department by ID
     */
    @Transactional
    public MessageResponse deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            return new MessageResponse("Department not found", false);
        }

        // Check if department has subdepartments
        if (!departmentRepository.findByParentId(id).isEmpty()) {
            return new MessageResponse("Cannot delete department with subdepartments", false);
        }

        departmentRepository.deleteById(id);

        return new MessageResponse("Department deleted successfully", true);
    }

    /**
     * Convert Department entity to DTO
     */
    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setCode(department.getCode());
        dto.setCompanyId(department.getCompany().getId());
        
        if (department.getParent() != null) {
            dto.setParentId(department.getParent().getId());
        }
        
        return dto;
    }
}