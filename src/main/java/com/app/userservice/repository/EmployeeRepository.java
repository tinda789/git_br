package com.app.userservice.repository;

import com.app.userservice.entity.organization.Company;
import com.app.userservice.entity.organization.Employee;
import com.app.userservice.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUser(User user);
    Optional<Employee> findByUserId(Long userId);
    Optional<Employee> findByEmployeeId(String employeeId);
    List<Employee> findByCompany(Company company);
    List<Employee> findByCompanyId(Long companyId);
    List<Employee> findByManager(Employee manager);
    List<Employee> findByManagerId(Long managerId);
    
    // Tìm nhân viên theo phòng ban chính
    List<Employee> findByDepartmentId(Long departmentId);
    
    // Tìm nhân viên theo phòng ban phụ
    @Query("SELECT e FROM Employee e JOIN e.secondaryDepartments d WHERE d.id = :departmentId")
    List<Employee> findBySecondaryDepartmentId(@Param("departmentId") Long departmentId);
    
    // Tìm tất cả nhân viên thuộc một phòng ban (chính hoặc phụ)
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId OR :departmentId IN (SELECT d.id FROM e.secondaryDepartments d)")
    List<Employee> findByAnyDepartmentId(@Param("departmentId") Long departmentId);
    
    // Tìm nhân viên theo vị trí
    List<Employee> findByPositionId(Long positionId);
    
    @Query("SELECT e FROM Employee e JOIN e.teams t WHERE t.id = :teamId")
    List<Employee> findByTeamId(@Param("teamId") Long teamId);
    
    List<Employee> findByStatus(int status);
    
    boolean existsByEmployeeId(String employeeId);
    boolean existsByUserId(Long userId);
    boolean existsByWorkEmail(String workEmail);
    
    // Đếm số nhân viên theo công ty
    long countByCompanyId(Long companyId);
    
    // Đếm số nhân viên theo công ty và trạng thái
    long countByCompanyIdAndStatus(Long companyId, int status);
    
    // Đếm số nhân viên theo phòng ban (chính hoặc phụ)
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId OR :departmentId IN (SELECT d.id FROM e.secondaryDepartments d)")
    long countByDepartmentIds(@Param("departmentId") Long departmentId);
}