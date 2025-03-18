package com.app.userservice.repository;

import com.app.userservice.entity.organization.Company;
import com.app.userservice.entity.organization.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByCompany(Company company);
    List<Department> findByCompanyId(Long companyId);
    List<Department> findByParentId(Long parentId);
    List<Department> findByParentIsNull();
    Optional<Department> findByNameAndCompanyId(String name, Long companyId);
    Optional<Department> findByCodeAndCompanyId(String code, Long companyId);
    boolean existsByNameAndCompanyId(String name, Long companyId);
    boolean existsByCodeAndCompanyId(String code, Long companyId);
    
    // Đếm số phòng ban theo công ty
    long countByCompanyId(Long companyId);
}