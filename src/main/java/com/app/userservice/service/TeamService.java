package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.TeamDTO;
import com.app.userservice.entity.organization.Department;
import com.app.userservice.entity.organization.Team;
import com.app.userservice.repository.DepartmentRepository;
import com.app.userservice.repository.TeamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Get all teams
     */
    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active teams
     */
    public List<TeamDTO> getActiveTeams() {
        return teamRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get teams by department ID
     */
    public List<TeamDTO> getTeamsByDepartment(Long departmentId) {
        return teamRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get team by ID
     */
    public TeamDTO getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        return convertToDTO(team);
    }

    /**
     * Create a new team
     */
    @Transactional
    public MessageResponse createTeam(TeamDTO teamDTO, Long userId) {
        // Check if department exists
        Department department = departmentRepository.findById(teamDTO.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Check if team name exists in the department
        if (teamRepository.existsByNameAndDepartmentId(
                teamDTO.getName(), teamDTO.getDepartmentId())) {
            return new MessageResponse("Team name already exists in this department", false);
        }

        // Create new team
        Team team = new Team();
        team.setName(teamDTO.getName());
        team.setDescription(teamDTO.getDescription());
        team.setDepartment(department);
        team.setActive(teamDTO.isActive());
        team.setCreatedAt(LocalDateTime.now());
        team.setCreatedBy(userId);

        teamRepository.save(team);

        return new MessageResponse("Team created successfully", true);
    }

    /**
     * Update an existing team
     */
    @Transactional
    public MessageResponse updateTeam(Long id, TeamDTO teamDTO, Long userId) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Check if department exists
        Department department = departmentRepository.findById(teamDTO.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Check if team name exists in the department
        if (!team.getName().equals(teamDTO.getName()) &&
                teamRepository.existsByNameAndDepartmentId(
                        teamDTO.getName(), teamDTO.getDepartmentId())) {
            return new MessageResponse("Team name already exists in this department", false);
        }

        // Update team
        team.setName(teamDTO.getName());
        team.setDescription(teamDTO.getDescription());
        team.setDepartment(department);
        team.setActive(teamDTO.isActive());
        team.setUpdatedAt(LocalDateTime.now());
        team.setUpdatedBy(userId);

        teamRepository.save(team);

        return new MessageResponse("Team updated successfully", true);
    }

    /**
     * Delete team by ID
     */
    @Transactional
    public MessageResponse deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            return new MessageResponse("Team not found", false);
        }

        // Perform soft delete by setting active to false
        Team team = teamRepository.findById(id).get();
        team.setActive(false);
        teamRepository.save(team);

        return new MessageResponse("Team deactivated successfully", true);
    }

    /**
     * Convert Team entity to DTO
     */
    private TeamDTO convertToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        dto.setDepartmentId(team.getDepartment().getId());
        dto.setActive(team.isActive());
        return dto;
    }
}