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
    
    @Query("SELECT e FROM Employee e JOIN e.departments d WHERE d.id = :departmentId")
    List<Employee> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT e FROM Employee e JOIN e.teams t WHERE t.id = :teamId")
    List<Employee> findByTeamId(@Param("teamId") Long teamId);
    
    @Query("SELECT e FROM Employee e JOIN e.positions p WHERE p.id = :positionId")
    List<Employee> findByPositionId(@Param("positionId") Long positionId);
    
    List<Employee> findByStatus(int status);
    
    boolean existsByEmployeeId(String employeeId);
    boolean existsByUserId(Long userId);
    boolean existsByWorkEmail(String workEmail);
}