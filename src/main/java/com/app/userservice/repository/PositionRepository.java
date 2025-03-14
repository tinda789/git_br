package com.app.userservice.repository;

import com.app.userservice.entity.organization.Department;
import com.app.userservice.entity.organization.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByDepartment(Department department);
    List<Position> findByDepartmentId(Long departmentId);
    List<Position> findByActiveTrue();
    List<Position> findByLevel(Integer level);
    Optional<Position> findByNameAndDepartmentId(String name, Long departmentId);
    boolean existsByNameAndDepartmentId(String name, Long departmentId);
}