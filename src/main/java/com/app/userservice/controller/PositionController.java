package com.app.userservice.controller;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.PositionDTO;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.service.PositionService;
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
@RequestMapping("/api/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PositionDTO>> getAllPositions() {
        List<PositionDTO> positions = positionService.getAllPositions();
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PositionDTO>> getActivePositions() {
        List<PositionDTO> positions = positionService.getActivePositions();
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PositionDTO>> getPositionsByDepartment(@PathVariable Long departmentId) {
        List<PositionDTO> positions = positionService.getPositionsByDepartment(departmentId);
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/level/{level}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PositionDTO>> getPositionsByLevel(@PathVariable Integer level) {
        List<PositionDTO> positions = positionService.getPositionsByLevel(level);
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PositionDTO> getPositionById(@PathVariable Long id) {
        PositionDTO position = positionService.getPositionById(id);
        return ResponseEntity.ok(position);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> createPosition(@Valid @RequestBody PositionDTO positionDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = positionService.createPosition(positionDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody PositionDTO positionDTO) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = positionService.updatePosition(id, positionDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> deletePosition(@PathVariable Long id) {
        MessageResponse response = positionService.deletePosition(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}