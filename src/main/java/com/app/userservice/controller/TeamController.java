package com.app.userservice.controller;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.TeamDTO;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.service.TeamService;
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
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        List<TeamDTO> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TeamDTO>> getActiveTeams() {
        List<TeamDTO> teams = teamService.getActiveTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TeamDTO>> getTeamsByDepartment(@PathVariable Long departmentId) {
        List<TeamDTO> teams = teamService.getTeamsByDepartment(departmentId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Long id) {
        TeamDTO team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> createTeam(@Valid @RequestBody TeamDTO teamDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = teamService.createTeam(teamDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamDTO teamDTO) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = teamService.updateTeam(id, teamDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> deleteTeam(@PathVariable Long id) {
        MessageResponse response = teamService.deleteTeam(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}