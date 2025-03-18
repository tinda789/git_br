package com.app.userservice.repository;

import com.app.userservice.entity.organization.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByDepartmentId(Long departmentId);
    List<Workspace> findByActiveTrue();
    boolean existsByNameAndDepartmentId(String name, Long departmentId);
}