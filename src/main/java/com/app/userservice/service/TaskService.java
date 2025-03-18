package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.TaskAttachmentDTO;
import com.app.userservice.dto.TaskCommentDTO;
import com.app.userservice.dto.TaskDTO;
import com.app.userservice.entity.organization.Department;
import com.app.userservice.entity.organization.Employee;
import com.app.userservice.entity.organization.Workspace;
import com.app.userservice.entity.task.Task;
import com.app.userservice.entity.task.TaskAttachment;
import com.app.userservice.entity.task.TaskComment;
import com.app.userservice.entity.task.TaskStatus;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskCommentRepository taskCommentRepository;

    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    /**
     * Get all tasks
     */
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by creator
     */
    public List<TaskDTO> getTasksByCreator(Long creatorId) {
        return taskRepository.findByCreatorId(creatorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by assignee
     */
    public List<TaskDTO> getTasksByAssignee(Long assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by workspace
     */
    public List<TaskDTO> getTasksByWorkspace(Long workspaceId) {
        return taskRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by department
     */
    public List<TaskDTO> getTasksByDepartment(Long departmentId) {
        return taskRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by status
     */
    public List<TaskDTO> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by assignee and status
     */
    public List<TaskDTO> getTasksByAssigneeAndStatus(Long assigneeId, TaskStatus status) {
        return taskRepository.findByAssigneeIdAndStatus(assigneeId, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get task by ID
     */
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return convertToDTO(task);
    }

    /**
     * Create a new task
     */
    @Transactional
    public MessageResponse createTask(TaskDTO taskDTO, Long creatorUserId) {
        // Get creator user
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("Creator user not found"));

        // Create new task
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setDueDate(taskDTO.getDueDate());
        task.setPriority(taskDTO.getPriority());
        task.setStatus(TaskStatus.TODO);
        task.setCreator(creator);
        task.setProgress(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setCreatedBy(creatorUserId);

        // Set workspace if provided
        if (taskDTO.getWorkspaceId() != null) {
            Workspace workspace = workspaceRepository.findById(taskDTO.getWorkspaceId())
                    .orElseThrow(() -> new RuntimeException("Workspace not found"));
            task.setWorkspace(workspace);
        }

        // Set department if provided
        if (taskDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(taskDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            task.setDepartment(department);
        }

        // Set assignee if provided
        if (taskDTO.getAssigneeId() != null) {
            Employee assignee = employeeRepository.findById(taskDTO.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        }

        taskRepository.save(task);

        return new MessageResponse("Task created successfully", true);
    }

    /**
     * Update an existing task
     */
    @Transactional
    public MessageResponse updateTask(Long id, TaskDTO taskDTO, Long updaterUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Update task fields
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setDueDate(taskDTO.getDueDate());
        task.setPriority(taskDTO.getPriority());
        task.setProgress(taskDTO.getProgress());
        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(updaterUserId);

        // Update status if changed
        if (taskDTO.getStatus() != task.getStatus()) {
            task.setStatus(taskDTO.getStatus());
            
            // Set completion date if task is done
            if (taskDTO.getStatus() == TaskStatus.DONE) {
                task.setCompletedAt(LocalDateTime.now());
            } else {
                task.setCompletedAt(null);
            }
        }

        // Update workspace if provided
        if (taskDTO.getWorkspaceId() != null) {
            Workspace workspace = workspaceRepository.findById(taskDTO.getWorkspaceId())
                    .orElseThrow(() -> new RuntimeException("Workspace not found"));
            task.setWorkspace(workspace);
        } else {
            task.setWorkspace(null);
        }

        // Update department if provided
        if (taskDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(taskDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            task.setDepartment(department);
        } else {
            task.setDepartment(null);
        }

        // Update assignee if provided
        if (taskDTO.getAssigneeId() != null) {
            Employee assignee = employeeRepository.findById(taskDTO.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        taskRepository.save(task);

        return new MessageResponse("Task updated successfully", true);
    }

    /**
     * Update task status
     */
    @Transactional
    public MessageResponse updateTaskStatus(Long id, TaskStatus status, Long updaterUserId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(updaterUserId);
        
        // Set completion date if task is done
        if (status == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        taskRepository.save(task);

        return new MessageResponse("Task status updated successfully", true);
    }

    /**
     * Update task progress
     */
    @Transactional
    public MessageResponse updateTaskProgress(Long id, int progress, Long updaterUserId) {
        if (progress < 0 || progress > 100) {
            return new MessageResponse("Progress must be between 0 and 100", false);
        }
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setProgress(progress);
        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(updaterUserId);
        
        // Auto update status based on progress
        if (progress == 100 && task.getStatus() != TaskStatus.DONE) {
            task.setStatus(TaskStatus.DONE);
            task.setCompletedAt(LocalDateTime.now());
        } else if (progress > 0 && progress < 100 && task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        taskRepository.save(task);

        return new MessageResponse("Task progress updated successfully", true);
    }

    /**
     * Add a comment to a task
     */
    @Transactional
    public MessageResponse addComment(Long taskId, TaskCommentDTO commentDTO, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
                
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(commentDTO.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        
        taskCommentRepository.save(comment);
        
        return new MessageResponse("Comment added successfully", true);
    }
    
    /**
     * Add an attachment to a task
     */
    @Transactional
    public MessageResponse addAttachment(Long taskId, TaskAttachmentDTO attachmentDTO, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
                
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTask(task);
        attachment.setUser(user);
        attachment.setFileName(attachmentDTO.getFileName());
        attachment.setFilePath(attachmentDTO.getFilePath());
        attachment.setFileType(attachmentDTO.getFileType());
        attachment.setFileSize(attachmentDTO.getFileSize());
        attachment.setUploadedAt(LocalDateTime.now());
        
        taskAttachmentRepository.save(attachment);
        
        return new MessageResponse("Attachment added successfully", true);
    }
    
    /**
     * Get comments for a task
     */
    public List<TaskCommentDTO> getTaskComments(Long taskId) {
        List<TaskComment> comments = taskCommentRepository.findByTaskId(taskId);
        
        return comments.stream()
                .map(this::convertCommentToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get attachments for a task
     */
    public List<TaskAttachmentDTO> getTaskAttachments(Long taskId) {
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        
        return attachments.stream()
                .map(this::convertAttachmentToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Task entity to DTO
     */
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setProgress(task.getProgress());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCreatedBy(task.getCreatedBy());
        
        if (task.getWorkspace() != null) {
            dto.setWorkspaceId(task.getWorkspace().getId());
        }
        
        if (task.getDepartment() != null) {
            dto.setDepartmentId(task.getDepartment().getId());
        }
        
        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
        }
        
        return dto;
    }
    
    /**
     * Convert TaskComment entity to DTO
     */
    private TaskCommentDTO convertCommentToDTO(TaskComment comment) {
        TaskCommentDTO dto = new TaskCommentDTO();
        dto.setId(comment.getId());
        dto.setTaskId(comment.getTask().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserFullName(comment.getUser().getFullName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
    
    /**
     * Convert TaskAttachment entity to DTO
     */
    private TaskAttachmentDTO convertAttachmentToDTO(TaskAttachment attachment) {
        TaskAttachmentDTO dto = new TaskAttachmentDTO();
        dto.setId(attachment.getId());
        dto.setTaskId(attachment.getTask().getId());
        dto.setUserId(attachment.getUser().getId());
        dto.setUserFullName(attachment.getUser().getFullName());
        dto.setFileName(attachment.getFileName());
        dto.setFilePath(attachment.getFilePath());
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        dto.setUploadedAt(attachment.getUploadedAt());
        return dto;
    }
}