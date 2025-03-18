package com.app.userservice.repository;

import com.app.userservice.entity.task.Task;
import com.app.userservice.entity.task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCreatorId(Long creatorId);
    List<Task> findByAssigneeId(Long assigneeId);
    List<Task> findByWorkspaceId(Long workspaceId);
    List<Task> findByDepartmentId(Long departmentId);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);
}