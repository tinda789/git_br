package com.app.userservice.controller;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.TaskDTO;
import com.app.userservice.entity.task.TaskStatus;
import com.app.userservice.security.service.UserDetailsImpl;
import com.app.userservice.service.TaskService;
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
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/created-by-me")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<List<TaskDTO>> getTasksCreatedByMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<TaskDTO> tasks = taskService.getTasksByCreator(userDetails.getId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<List<TaskDTO>> getTasksAssignedToMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Assuming we have a method to get employee ID by user ID
        // This would require additional service method
        // For now, we'll pass the user ID directly
        List<TaskDTO> tasks = taskService.getTasksByAssignee(userDetails.getId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/workspace/{workspaceId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<List<TaskDTO>> getTasksByWorkspace(@PathVariable Long workspaceId) {
        List<TaskDTO> tasks = taskService.getTasksByWorkspace(workspaceId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<List<TaskDTO>> getTasksByDepartment(@PathVariable Long departmentId) {
        List<TaskDTO> tasks = taskService.getTasksByDepartment(departmentId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(@PathVariable TaskStatus status) {
        List<TaskDTO> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assignee/{assigneeId}/status/{status}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<List<TaskDTO>> getTasksByAssigneeAndStatus(
            @PathVariable Long assigneeId, 
            @PathVariable TaskStatus status) {
        List<TaskDTO> tasks = taskService.getTasksByAssigneeAndStatus(assigneeId, status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<MessageResponse> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = taskService.createTask(taskDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<MessageResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDTO taskDTO) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = taskService.updateTask(id, taskDTO, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<MessageResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = taskService.updateTaskStatus(id, status, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/progress")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('USER')")
    public ResponseEntity<MessageResponse> updateTaskProgress(
            @PathVariable Long id,
            @RequestParam int progress) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        MessageResponse response = taskService.updateTaskProgress(id, progress, userDetails.getId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}