package com.app.userservice.dto;

import com.app.userservice.entity.task.TaskPriority;
import com.app.userservice.entity.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    
    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title cannot exceed 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    private Long workspaceId;
    
    private Long departmentId;
    
    private Long assigneeId;
    
    private LocalDate dueDate;
    
    private TaskPriority priority = TaskPriority.MEDIUM;
    
    private TaskStatus status = TaskStatus.TODO;
    
    private List<TaskCommentDTO> comments;
    
    private List<TaskAttachmentDTO> attachments;
    
    private Set<Long> watcherIds;
    
    private int progress = 0;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime completedAt;
    
    private Long createdBy;
}