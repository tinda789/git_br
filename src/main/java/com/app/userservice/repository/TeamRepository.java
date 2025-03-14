package com.app.userservice.repository;

import com.app.userservice.entity.organization.Department;
import com.app.userservice.entity.organization.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByDepartment(Department department);
    List<Team> findByDepartmentId(Long departmentId);
    List<Team> findByActiveTrue();
    Optional<Team> findByNameAndDepartmentId(String name, Long departmentId);
    boolean existsByNameAndDepartmentId(String name, Long departmentId);
}