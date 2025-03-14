package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.PositionDTO;
import com.app.userservice.entity.organization.Department;
import com.app.userservice.entity.organization.Position;
import com.app.userservice.repository.DepartmentRepository;
import com.app.userservice.repository.PositionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PositionService {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Get all positions
     */
    public List<PositionDTO> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active positions
     */
    public List<PositionDTO> getActivePositions() {
        return positionRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get positions by department ID
     */
    public List<PositionDTO> getPositionsByDepartment(Long departmentId) {
        return positionRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get positions by level
     */
    public List<PositionDTO> getPositionsByLevel(Integer level) {
        return positionRepository.findByLevel(level).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get position by ID
     */
    public PositionDTO getPositionById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found"));
        return convertToDTO(position);
    }

    /**
     * Create a new position
     */
    @Transactional
    public MessageResponse createPosition(PositionDTO positionDTO, Long userId) {
        // Check if department exists
        Department department = departmentRepository.findById(positionDTO.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Check if position name exists in the department
        if (positionRepository.existsByNameAndDepartmentId(
                positionDTO.getName(), positionDTO.getDepartmentId())) {
            return new MessageResponse("Position name already exists in this department", false);
        }

        // Create new position
        Position position = new Position();
        position.setName(positionDTO.getName());
        position.setDescription(positionDTO.getDescription());
        position.setLevel(positionDTO.getLevel());
        position.setDepartment(department);
        position.setActive(positionDTO.isActive());
        position.setCreatedAt(LocalDateTime.now());
        position.setCreatedBy(userId);

        positionRepository.save(position);

        return new MessageResponse("Position created successfully", true);
    }

    /**
     * Update an existing position
     */
    @Transactional
    public MessageResponse updatePosition(Long id, PositionDTO positionDTO, Long userId) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found"));

        // Check if department exists
        Department department = departmentRepository.findById(positionDTO.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Check if position name exists in the department
        if (!position.getName().equals(positionDTO.getName()) &&
                positionRepository.existsByNameAndDepartmentId(
                        positionDTO.getName(), positionDTO.getDepartmentId())) {
            return new MessageResponse("Position name already exists in this department", false);
        }

        // Update position
        position.setName(positionDTO.getName());
        position.setDescription(positionDTO.getDescription());
        position.setLevel(positionDTO.getLevel());
        position.setDepartment(department);
        position.setActive(positionDTO.isActive());
        position.setUpdatedAt(LocalDateTime.now());
        position.setUpdatedBy(userId);

        positionRepository.save(position);

        return new MessageResponse("Position updated successfully", true);
    }

    /**
     * Delete position by ID
     */
    @Transactional
    public MessageResponse deletePosition(Long id) {
        if (!positionRepository.existsById(id)) {
            return new MessageResponse("Position not found", false);
        }

        // Perform soft delete by setting active to false
        Position position = positionRepository.findById(id).get();
        position.setActive(false);
        positionRepository.save(position);

        return new MessageResponse("Position deactivated successfully", true);
    }

    /**
     * Convert Position entity to DTO
     */
    private PositionDTO convertToDTO(Position position) {
        PositionDTO dto = new PositionDTO();
        dto.setId(position.getId());
        dto.setName(position.getName());
        dto.setDescription(position.getDescription());
        dto.setLevel(position.getLevel());
        dto.setDepartmentId(position.getDepartment().getId());
        dto.setActive(position.isActive());
        return dto;
    }
}