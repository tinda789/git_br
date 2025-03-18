package com.app.userservice.service;

import com.app.userservice.dto.EmployeeDTO;
import com.app.userservice.dto.MessageResponse;
import com.app.userservice.entity.organization.*;
import com.app.userservice.entity.user.User;
import com.app.userservice.exception.ResourceNotFoundException;
import com.app.userservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    * Lấy tất cả nhân viên
    */
   public List<EmployeeDTO> getAllEmployees() {
       return employeeRepository.findAll().stream()
               .map(this::convertToDTO)
               .collect(Collectors.toList());
   }

   /**
    * Lấy nhân viên theo công ty
    */
   public List<EmployeeDTO> getEmployeesByCompany(Long companyId) {
       return employeeRepository.findByCompanyId(companyId).stream()
               .map(this::convertToDTO)
               .collect(Collectors.toList());
   }

   /**
    * Lấy nhân viên theo phòng ban chính
    */
   public List<EmployeeDTO> getEmployeesByDepartment(Long departmentId) {
       return employeeRepository.findByDepartmentId(departmentId).stream()
               .map(this::convertToDTO)
               .collect(Collectors.toList());
   }
   
   /**
    * Lấy nhân viên theo phòng ban phụ
    */
   public List<EmployeeDTO> getEmployeesBySecondaryDepartment(Long departmentId) {
       return employeeRepository.findBySecondaryDepartmentId(departmentId).stream()
               .map(this::convertToDTO)
               .collect(Collectors.toList());
   }
   
   /**
    * Lấy nhân viên theo phòng ban bất kỳ (chính hoặc phụ)
    */
   public List<EmployeeDTO> getEmployeesByAnyDepartment(Long departmentId) {
       return employeeRepository.findByAnyDepartmentId(departmentId).stream()
               .map(this::convertToDTO)
               .collect(Collectors.toList());
   }

   /**
    * Lấy nhân viên theo nhóm
    */
   public List<EmployeeDTO> getEmployeesByTeam(Long teamId) {
       return employeeRepository.findByTeamId(teamId).stream()
               .map(this::convertToDTO)
               .collect(Collectors.toList());
   }

   /**
    * Lấy nhân viên theo vị trí
    */
   public List<EmployeeDTO> getEmployeesByPosition(Long positionId) {
       return employeeRepository.findByPositionId(positionId).stream()
               .map(this::convertToDTO)
               .collect(Collectors.toList());
   }

   /**
    * Lấy nhân viên theo quản lý
    */
   public List<EmployeeDTO> getEmployeesByManager(Long managerId) {
       return employeeRepository.findByManagerId(managerId).stream()
               .map(this::convertToDTO)
               .collect(Collectors.toList());
   }

   /**
    * Lấy nhân viên theo ID
    */
   public EmployeeDTO getEmployeeById(Long id) {
       Employee employee = employeeRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", id));
       return convertToDTO(employee);
   }

   /**
    * Lấy nhân viên theo ID người dùng
    */
   public EmployeeDTO getEmployeeByUserId(Long userId) {
       Employee employee = employeeRepository.findByUserId(userId)
               .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "userId", userId));
       return convertToDTO(employee);
   }

   /**
    * Tạo nhân viên mới
    */
   @Transactional
   public MessageResponse createEmployee(EmployeeDTO employeeDTO, Long creatorUserId) {
       // Kiểm tra người dùng
       User user = userRepository.findById(employeeDTO.getUserId())
               .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", employeeDTO.getUserId()));

       // Kiểm tra người dùng đã có hồ sơ nhân viên
       if (employeeRepository.existsByUserId(employeeDTO.getUserId())) {
           return new MessageResponse("Người dùng này đã có hồ sơ nhân viên", false);
       }

       // Kiểm tra công ty
       Company company = companyRepository.findById(employeeDTO.getCompanyId())
               .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", employeeDTO.getCompanyId()));
       
       // Kiểm tra phòng ban chính
       Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
               .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", employeeDTO.getDepartmentId()));
       
       // Kiểm tra vị trí
       Position position = positionRepository.findById(employeeDTO.getPositionId())
               .orElseThrow(() -> new ResourceNotFoundException("Vị trí", "id", employeeDTO.getPositionId()));

       // Kiểm tra mã nhân viên
       if (employeeDTO.getEmployeeId() != null && !employeeDTO.getEmployeeId().isEmpty() &&
               employeeRepository.existsByEmployeeId(employeeDTO.getEmployeeId())) {
           return new MessageResponse("Mã nhân viên đã tồn tại", false);
       }

       // Kiểm tra email công việc
       if (employeeDTO.getWorkEmail() != null && !employeeDTO.getWorkEmail().isEmpty() &&
               employeeRepository.existsByWorkEmail(employeeDTO.getWorkEmail())) {
           return new MessageResponse("Email công việc đã tồn tại", false);
       }

       // Tạo nhân viên mới
       Employee employee = new Employee();
       employee.setEmployeeId(employeeDTO.getEmployeeId());
       employee.setUser(user);
       employee.setCompany(company);
       employee.setDepartment(department);
       employee.setPosition(position);
       employee.setHireDate(employeeDTO.getHireDate());
       employee.setTerminationDate(employeeDTO.getTerminationDate());
       employee.setStatus(employeeDTO.getStatus());
       employee.setJobTitle(employeeDTO.getJobTitle());
       employee.setWorkEmail(employeeDTO.getWorkEmail());
       employee.setWorkPhone(employeeDTO.getWorkPhone());
       employee.setCreatedAt(LocalDateTime.now());
       employee.setCreatedBy(creatorUserId);

       // Đặt quản lý
       if (employeeDTO.getManagerId() != null) {
           Employee manager = employeeRepository.findById(employeeDTO.getManagerId())
                   .orElseThrow(() -> new ResourceNotFoundException("Quản lý", "id", employeeDTO.getManagerId()));
           employee.setManager(manager);
       }

       // Đặt phòng ban phụ
       if (employeeDTO.getSecondaryDepartmentIds() != null && !employeeDTO.getSecondaryDepartmentIds().isEmpty()) {
           Set<Department> secondaryDepartments = new HashSet<>();
           for (Long deptId : employeeDTO.getSecondaryDepartmentIds()) {
               if (!deptId.equals(employeeDTO.getDepartmentId())) { // Không thêm phòng ban chính làm phòng ban phụ
                   Department dept = departmentRepository.findById(deptId)
                           .orElseThrow(() -> new ResourceNotFoundException("Phòng ban phụ", "id", deptId));
                   secondaryDepartments.add(dept);
               }
           }
           employee.setSecondaryDepartments(secondaryDepartments);
       }

       // Đặt nhóm
       if (employeeDTO.getTeamIds() != null && !employeeDTO.getTeamIds().isEmpty()) {
           Set<Team> teams = new HashSet<>();
           for (Long teamId : employeeDTO.getTeamIds()) {
               Team team = teamRepository.findById(teamId)
                       .orElseThrow(() -> new ResourceNotFoundException("Nhóm", "id", teamId));
               teams.add(team);
           }
           employee.setTeams(teams);
       }

       employeeRepository.save(employee);

       return new MessageResponse("Tạo nhân viên thành công", true);
   }

   /**
    * Cập nhật nhân viên
    */
   @Transactional
   public MessageResponse updateEmployee(Long id, EmployeeDTO employeeDTO, Long updaterUserId) {
       Employee employee = employeeRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", id));

       // Kiểm tra công ty
       Company company = companyRepository.findById(employeeDTO.getCompanyId())
               .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", employeeDTO.getCompanyId()));
       
       // Kiểm tra phòng ban chính
       Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
               .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", employeeDTO.getDepartmentId()));
       
       // Kiểm tra vị trí
       Position position = positionRepository.findById(employeeDTO.getPositionId())
               .orElseThrow(() -> new ResourceNotFoundException("Vị trí", "id", employeeDTO.getPositionId()));

       // Kiểm tra mã nhân viên
       if (employeeDTO.getEmployeeId() != null && !employeeDTO.getEmployeeId().isEmpty() &&
               !employeeDTO.getEmployeeId().equals(employee.getEmployeeId()) &&
               employeeRepository.existsByEmployeeId(employeeDTO.getEmployeeId())) {
           return new MessageResponse("Mã nhân viên đã tồn tại", false);
       }

       // Kiểm tra email công việc
       if (employeeDTO.getWorkEmail() != null && !employeeDTO.getWorkEmail().isEmpty() &&
               !employeeDTO.getWorkEmail().equals(employee.getWorkEmail()) &&
               employeeRepository.existsByWorkEmail(employeeDTO.getWorkEmail())) {
           return new MessageResponse("Email công việc đã tồn tại", false);
       }

       // Cập nhật thông tin nhân viên
       employee.setEmployeeId(employeeDTO.getEmployeeId());
       employee.setCompany(company);
       employee.setDepartment(department);
       employee.setPosition(position);
       employee.setHireDate(employeeDTO.getHireDate());
       employee.setTerminationDate(employeeDTO.getTerminationDate());
       employee.setStatus(employeeDTO.getStatus());
       employee.setJobTitle(employeeDTO.getJobTitle());
       employee.setWorkEmail(employeeDTO.getWorkEmail());
       employee.setWorkPhone(employeeDTO.getWorkPhone());
       employee.setUpdatedAt(LocalDateTime.now());
       employee.setUpdatedBy(updaterUserId);

       // Đặt quản lý
       if (employeeDTO.getManagerId() != null) {
           // Ngăn chặn việc tự quản lý
           if (employeeDTO.getManagerId().equals(id)) {
               return new MessageResponse("Nhân viên không thể tự quản lý mình", false);
           }
           
           Employee manager = employeeRepository.findById(employeeDTO.getManagerId())
                   .orElseThrow(() -> new ResourceNotFoundException("Quản lý", "id", employeeDTO.getManagerId()));
           employee.setManager(manager);
       } else {
           employee.setManager(null);
       }

       // Cập nhật phòng ban phụ
       if (employeeDTO.getSecondaryDepartmentIds() != null) {
           Set<Department> secondaryDepartments = new HashSet<>();
           for (Long deptId : employeeDTO.getSecondaryDepartmentIds()) {
               if (!deptId.equals(employeeDTO.getDepartmentId())) { // Không thêm phòng ban chính làm phòng ban phụ
                   Department dept = departmentRepository.findById(deptId)
                           .orElseThrow(() -> new ResourceNotFoundException("Phòng ban phụ", "id", deptId));
                   secondaryDepartments.add(dept);
               }
           }
           employee.setSecondaryDepartments(secondaryDepartments);
       }

       // Cập nhật nhóm
       if (employeeDTO.getTeamIds() != null) {
           Set<Team> teams = new HashSet<>();
           for (Long teamId : employeeDTO.getTeamIds()) {
               Team team = teamRepository.findById(teamId)
                       .orElseThrow(() -> new ResourceNotFoundException("Nhóm", "id", teamId));
               teams.add(team);
           }
           employee.setTeams(teams);
       }

       employeeRepository.save(employee);

       return new MessageResponse("Cập nhật nhân viên thành công", true);
   }

   /**
    * Vô hiệu hóa nhân viên
    */
   @Transactional
   public MessageResponse deactivateEmployee(Long id) {
       Employee employee = employeeRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", id));

       // Đặt trạng thái thành không hoạt động (2 = không hoạt động)
       employee.setStatus(2);
       employeeRepository.save(employee);

       return new MessageResponse("Vô hiệu hóa nhân viên thành công", true);
   }

   /**
    * Chấm dứt hợp đồng nhân viên
    */
   @Transactional
   public MessageResponse terminateEmployee(Long id, LocalDateTime terminationDate) {
       Employee employee = employeeRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", id));

       // Đặt trạng thái thành đã chấm dứt (3 = đã chấm dứt)
       employee.setStatus(3);
       employee.setTerminationDate(terminationDate.toLocalDate());
       employeeRepository.save(employee);

       return new MessageResponse("Chấm dứt hợp đồng nhân viên thành công", true);
   }

   /**
    * Chuyển đổi từ Entity sang DTO
    */
   private EmployeeDTO convertToDTO(Employee employee) {
       EmployeeDTO dto = new EmployeeDTO();
       dto.setId(employee.getId());
       dto.setEmployeeId(employee.getEmployeeId());
       dto.setUserId(employee.getUser().getId());
       dto.setCompanyId(employee.getCompany().getId());
       dto.setDepartmentId(employee.getDepartment().getId());
       dto.setPositionId(employee.getPosition().getId());
       dto.setHireDate(employee.getHireDate());
       dto.setTerminationDate(employee.getTerminationDate());
       dto.setStatus(employee.getStatus());
       dto.setJobTitle(employee.getJobTitle());
       dto.setWorkEmail(employee.getWorkEmail());
       dto.setWorkPhone(employee.getWorkPhone());
       
       if (employee.getManager() != null) {
           dto.setManagerId(employee.getManager().getId());
       }
       
       // Lấy ID các phòng ban phụ
       Set<Long> secondaryDeptIds = employee.getSecondaryDepartments().stream()
               .map(Department::getId)
               .collect(Collectors.toSet());
       dto.setSecondaryDepartmentIds(secondaryDeptIds);
       
       // Lấy ID các nhóm
       Set<Long> teamIds = employee.getTeams().stream()
               .map(Team::getId)
               .collect(Collectors.toSet());
       dto.setTeamIds(teamIds);
       
       return dto;
   }
}