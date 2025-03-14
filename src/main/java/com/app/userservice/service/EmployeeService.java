package com.app.userservice.service;

import com.app.userservice.dto.EmployeeDTO;
import com.app.userservice.dto.MessageResponse;
import com.app.userservice.entity.organization.*;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PositionRepository positionRepository;

    /**
     * Get all employees
     */
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get employees by company ID
     */
    public List<EmployeeDTO> getEmployeesByCompany(Long companyId) {
        return employeeRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get employees by department ID
     */
    public List<EmployeeDTO> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get employees by team ID
     */
    public List<EmployeeDTO> getEmployeesByTeam(Long teamId) {
        return employeeRepository.findByTeamId(teamId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get employees by position ID
     */
    public List<EmployeeDTO> getEmployeesByPosition(Long positionId) {
        return employeeRepository.findByPositionId(positionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get employees by manager ID
     */
    public List<EmployeeDTO> getEmployeesByManager(Long managerId) {
        return employeeRepository.findByManagerId(managerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get employee by ID
     */
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToDTO(employee);
    }

    /**
     * Get employee by user ID
     */
    public EmployeeDTO getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee not found for this user"));
        return convertToDTO(employee);
    }

    /**
     * Create a new employee
     */
    @Transactional
    public MessageResponse createEmployee(EmployeeDTO employeeDTO, Long creatorUserId) {
        // Check if user exists
        User user = userRepository.findById(employeeDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already has an employee record
        if (employeeRepository.existsByUserId(employeeDTO.getUserId())) {
            return new MessageResponse("This user already has an employee record", false);
        }

        // Check if company exists
        Company company = companyRepository.findById(employeeDTO.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Check if employee ID is unique if provided
        if (employeeDTO.getEmployeeId() != null && !employeeDTO.getEmployeeId().isEmpty() &&
                employeeRepository.existsByEmployeeId(employeeDTO.getEmployeeId())) {
            return new MessageResponse("Employee ID already exists", false);
        }

        // Check if work email is unique if provided
        if (employeeDTO.getWorkEmail() != null && !employeeDTO.getWorkEmail().isEmpty() &&
                employeeRepository.existsByWorkEmail(employeeDTO.getWorkEmail())) {
            return new MessageResponse("Work email already exists", false);
        }

        // Create new employee
        Employee employee = new Employee();
        employee.setEmployeeId(employeeDTO.getEmployeeId());
        employee.setUser(user);
        employee.setCompany(company);
        employee.setHireDate(employeeDTO.getHireDate());
        employee.setTerminationDate(employeeDTO.getTerminationDate());
        employee.setStatus(employeeDTO.getStatus());
        employee.setJobTitle(employeeDTO.getJobTitle());
        employee.setWorkEmail(employeeDTO.getWorkEmail());
        employee.setWorkPhone(employeeDTO.getWorkPhone());
        employee.setCreatedAt(LocalDateTime.now());
        employee.setCreatedBy(creatorUserId);

        // Set manager if provided
        if (employeeDTO.getManagerId() != null) {
            Employee manager = employeeRepository.findById(employeeDTO.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            employee.setManager(manager);
        }

        // Set departments
        if (employeeDTO.getDepartmentIds() != null && !employeeDTO.getDepartmentIds().isEmpty()) {
            Set<Department> departments = new HashSet<>();
            for (Long departmentId : employeeDTO.getDepartmentIds()) {
                Department department = departmentRepository.findById(departmentId)
                        .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));
                departments.add(department);
            }
            employee.setDepartments(departments);
        }

        // Set teams
        if (employeeDTO.getTeamIds() != null && !employeeDTO.getTeamIds().isEmpty()) {
            Set<Team> teams = new HashSet<>();
            for (Long teamId : employeeDTO.getTeamIds()) {
                Team team = teamRepository.findById(teamId)
                        .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
                teams.add(team);
            }
            employee.setTeams(teams);
        }

        // Set positions
        if (employeeDTO.getPositionIds() != null && !employeeDTO.getPositionIds().isEmpty()) {
            Set<Position> positions = new HashSet<>();
            for (Long positionId : employeeDTO.getPositionIds()) {
                Position position = positionRepository.findById(positionId)
                        .orElseThrow(() -> new RuntimeException("Position not found: " + positionId));
                positions.add(position);
            }
            employee.setPositions(positions);
        }

        employeeRepository.save(employee);

        return new MessageResponse("Employee created successfully", true);
    }

    /**
     * Update an existing employee
     */
    @Transactional
    public MessageResponse updateEmployee(Long id, EmployeeDTO employeeDTO, Long updaterUserId) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Check if company exists
        Company company = companyRepository.findById(employeeDTO.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Check if employee ID is unique if changed
        if (employeeDTO.getEmployeeId() != null && !employeeDTO.getEmployeeId().isEmpty() &&
                !employeeDTO.getEmployeeId().equals(employee.getEmployeeId()) &&
                employeeRepository.existsByEmployeeId(employeeDTO.getEmployeeId())) {
            return new MessageResponse("Employee ID already exists", false);
        }

        // Check if work email is unique if changed
        if (employeeDTO.getWorkEmail() != null && !employeeDTO.getWorkEmail().isEmpty() &&
                !employeeDTO.getWorkEmail().equals(employee.getWorkEmail()) &&
                employeeRepository.existsByWorkEmail(employeeDTO.getWorkEmail())) {
            return new MessageResponse("Work email already exists", false);
        }

        // Update employee
        employee.setEmployeeId(employeeDTO.getEmployeeId());
        employee.setCompany(company);
        employee.setHireDate(employeeDTO.getHireDate());
        employee.setTerminationDate(employeeDTO.getTerminationDate());
        employee.setStatus(employeeDTO.getStatus());
        employee.setJobTitle(employeeDTO.getJobTitle());
        employee.setWorkEmail(employeeDTO.getWorkEmail());
        employee.setWorkPhone(employeeDTO.getWorkPhone());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setUpdatedBy(updaterUserId);

        // Set manager if provided
        if (employeeDTO.getManagerId() != null) {
            // Prevent self-management
            if (employeeDTO.getManagerId().equals(id)) {
                return new MessageResponse("Employee cannot be their own manager", false);
            }
            
            Employee manager = employeeRepository.findById(employeeDTO.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            employee.setManager(manager);
        } else {
            employee.setManager(null);
        }

        // Update departments
        if (employeeDTO.getDepartmentIds() != null) {
            Set<Department> departments = new HashSet<>();
            for (Long departmentId : employeeDTO.getDepartmentIds()) {
                Department department = departmentRepository.findById(departmentId)
                        .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));
                departments.add(department);
            }
            employee.setDepartments(departments);
        }

        // Update teams
        if (employeeDTO.getTeamIds() != null) {
            Set<Team> teams = new HashSet<>();
            for (Long teamId : employeeDTO.getTeamIds()) {
                Team team = teamRepository.findById(teamId)
                        .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
                teams.add(team);
            }
            employee.setTeams(teams);
        }

        // Update positions
        if (employeeDTO.getPositionIds() != null) {
            Set<Position> positions = new HashSet<>();
            for (Long positionId : employeeDTO.getPositionIds()) {
                Position position = positionRepository.findById(positionId)
                        .orElseThrow(() -> new RuntimeException("Position not found: " + positionId));
                positions.add(position);
            }
            employee.setPositions(positions);
        }

        employeeRepository.save(employee);

        return new MessageResponse("Employee updated successfully", true);
    }

    /**
     * Deactivate employee by ID
     */
    @Transactional
    public MessageResponse deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Set status to inactive (2 = inactive)
        employee.setStatus(2);
        employeeRepository.save(employee);

        return new MessageResponse("Employee deactivated successfully", true);
    }

    /**
     * Terminate employee by ID
     */
    @Transactional
    public MessageResponse terminateEmployee(Long id, LocalDateTime terminationDate) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Set status to terminated (3 = terminated)
        employee.setStatus(3);
        employee.setTerminationDate(terminationDate.toLocalDate());
        employeeRepository.save(employee);

        return new MessageResponse("Employee terminated successfully", true);
    }

    /**
     * Convert Employee entity to DTO
     */
    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setUserId(employee.getUser().getId());
        dto.setCompanyId(employee.getCompany().getId());
        dto.setHireDate(employee.getHireDate());
        dto.setTerminationDate(employee.getTerminationDate());
        dto.setStatus(employee.getStatus());
        dto.setJobTitle(employee.getJobTitle());
        dto.setWorkEmail(employee.getWorkEmail());
        dto.setWorkPhone(employee.getWorkPhone());
        
        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getId());
        }
        
        // Get department IDs
        Set<Long> departmentIds = employee.getDepartments().stream()
                .map(Department::getId)
                .collect(Collectors.toSet());
        dto.setDepartmentIds(departmentIds);
        
        // Get team IDs
        Set<Long> teamIds = employee.getTeams().stream()
                .map(Team::getId)
                .collect(Collectors.toSet());
        dto.setTeamIds(teamIds);
        
        // Get position IDs
        Set<Long> positionIds = employee.getPositions().stream()
                .map(Position::getId)
                .collect(Collectors.toSet());
        dto.setPositionIds(positionIds);
        
        return dto;
    }
}