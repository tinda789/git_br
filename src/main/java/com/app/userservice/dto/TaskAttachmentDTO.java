package com.app.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentDTO {
    private Long id;
    
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    private Long userId;
    
    private String userFullName;
    
    private String fileName;
    
    private String filePath;
    
    private String fileType;
    
    private Long fileSize;
    
    private LocalDateTime uploadedAt;
}