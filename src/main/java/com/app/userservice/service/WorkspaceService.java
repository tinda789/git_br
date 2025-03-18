package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.WorkspaceDTO;
import com.app.userservice.entity.organization.Department;
import com.app.userservice.entity.organization.Workspace;
import com.app.userservice.repository.DepartmentRepository;
import com.app.userservice.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Get all workspaces
     */
    public List<WorkspaceDTO> getAllWorkspaces() {
        return workspaceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active workspaces
     */
    public List<WorkspaceDTO> getActiveWorkspaces() {
        return workspaceRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get workspaces by department ID
     */
    public List<WorkspaceDTO> getWorkspacesByDepartment(Long departmentId) {
        return workspaceRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get workspace by ID
     */
    public WorkspaceDTO getWorkspaceById(Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        return convertToDTO(workspace);
    }

    /**
     * Create a new workspace
     */
    @Transactional
    public MessageResponse createWorkspace(WorkspaceDTO workspaceDTO, Long userId) {
        // Check if department exists if provided
        Department department = null;
        if (workspaceDTO.getDepartmentId() != null) {
            department = departmentRepository.findById(workspaceDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
                    
            // Check if workspace name exists in the department
            if (workspaceRepository.existsByNameAndDepartmentId(
                    workspaceDTO.getName(), workspaceDTO.getDepartmentId())) {
                return new MessageResponse("Workspace name already exists in this department", false);
            }
        }

        // Create new workspace
        Workspace workspace = new Workspace();
        workspace.setName(workspaceDTO.getName());
        workspace.setDescription(workspaceDTO.getDescription());
        workspace.setIcon(workspaceDTO.getIcon());
        workspace.setDepartment(department);
        workspace.setActive(workspaceDTO.isActive());
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setCreatedBy(userId);

        workspaceRepository.save(workspace);

        return new MessageResponse("Workspace created successfully", true);
    }

    /**
     * Update an existing workspace
     */
    @Transactional
    public MessageResponse updateWorkspace(Long id, WorkspaceDTO workspaceDTO, Long userId) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        // Check if department exists if provided
        if (workspaceDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(workspaceDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
                    
            // Check if workspace name exists in the department
            if (!workspace.getName().equals(workspaceDTO.getName()) &&
                    workspaceRepository.existsByNameAndDepartmentId(
                            workspaceDTO.getName(), workspaceDTO.getDepartmentId())) {
                return new MessageResponse("Workspace name already exists in this department", false);
            }
            
            workspace.setDepartment(department);
        } else {
            workspace.setDepartment(null);
        }

        // Update workspace
        workspace.setName(workspaceDTO.getName());
        workspace.setDescription(workspaceDTO.getDescription());
        workspace.setIcon(workspaceDTO.getIcon());
        workspace.setActive(workspaceDTO.isActive());
        workspace.setUpdatedAt(LocalDateTime.now());
        workspace.setUpdatedBy(userId);

        workspaceRepository.save(workspace);

        return new MessageResponse("Workspace updated successfully", true);
    }

    /**
     * Delete workspace by ID
     */
    @Transactional
    public MessageResponse deleteWorkspace(Long id) {
        if (!workspaceRepository.existsById(id)) {
            return new MessageResponse("Workspace not found", false);
        }

        // Soft delete by setting active to false
        Workspace workspace = workspaceRepository.findById(id).get();
        workspace.setActive(false);
        workspaceRepository.save(workspace);

        return new MessageResponse("Workspace deactivated successfully", true);
    }

    /**
     * Convert Workspace entity to DTO
     */
    private WorkspaceDTO convertToDTO(Workspace workspace) {
        WorkspaceDTO dto = new WorkspaceDTO();
        dto.setId(workspace.getId());
        dto.setName(workspace.getName());
        dto.setDescription(workspace.getDescription());
        dto.setIcon(workspace.getIcon());
        
        if (workspace.getDepartment() != null) {
            dto.setDepartmentId(workspace.getDepartment().getId());
        }
        
        dto.setActive(workspace.isActive());
        return dto;
    }
}