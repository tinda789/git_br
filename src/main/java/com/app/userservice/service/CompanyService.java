package com.app.userservice.service;

import com.app.userservice.dto.CompanyDTO;
import com.app.userservice.dto.CompanyDetailDTO;
import com.app.userservice.dto.CompanyStatsDTO;
import com.app.userservice.dto.MessageResponse;
import com.app.userservice.entity.organization.Company;
import com.app.userservice.entity.organization.Department;
import com.app.userservice.entity.organization.Employee;
import com.app.userservice.exception.ResourceNotFoundException;
import com.app.userservice.repository.CompanyRepository;
import com.app.userservice.repository.DepartmentRepository;
import com.app.userservice.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Lấy tất cả công ty
     */
    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy công ty đang hoạt động
     */
    public List<CompanyDTO> getAllActiveCompanies() {
        return companyRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy công ty theo ID
     */
    public CompanyDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", id));
        return convertToDTO(company);
    }
    
    /**
     * Lấy thông tin chi tiết công ty bao gồm số lượng phòng ban, nhóm, nhân viên
     */
    public CompanyDetailDTO getCompanyDetail(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", id));
        
        CompanyDetailDTO detailDTO = new CompanyDetailDTO();
        detailDTO.setCompany(convertToDTO(company));
        
        // Lấy số lượng phòng ban
        long departmentCount = departmentRepository.countByCompanyId(id);
        detailDTO.setDepartmentCount(departmentCount);
        
        // Lấy số lượng nhân viên
        long employeeCount = employeeRepository.countByCompanyId(id);
        detailDTO.setEmployeeCount(employeeCount);
        
        // Lấy số lượng nhân viên đang hoạt động
        long activeEmployeeCount = employeeRepository.countByCompanyIdAndStatus(id, 1);
        detailDTO.setActiveEmployeeCount(activeEmployeeCount);
        
        return detailDTO;
    }
    
    /**
     * Lấy thống kê công ty
     */
    public CompanyStatsDTO getCompanyStats(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", id));
        
        CompanyStatsDTO statsDTO = new CompanyStatsDTO();
        statsDTO.setCompanyName(company.getName());
        
        // Lấy thống kê phòng ban
        long departmentCount = departmentRepository.countByCompanyId(id);
        statsDTO.setTotalDepartments(departmentCount);
        
        // Lấy thống kê nhân viên
        long employeeCount = employeeRepository.countByCompanyId(id);
        statsDTO.setTotalEmployees(employeeCount);
        
        // Phần trăm nhân viên theo phòng ban
        List<Department> departments = departmentRepository.findByCompanyId(id);
        Map<String, Double> departmentDistribution = new HashMap<>();
        
        for (Department dept : departments) {
            long deptEmployeeCount = employeeRepository.countByDepartmentIds(dept.getId());
            double percentage = employeeCount > 0 ? (double) deptEmployeeCount / employeeCount * 100 : 0;
            departmentDistribution.put(dept.getName(), Math.round(percentage * 10) / 10.0);
        }
        
        statsDTO.setDepartmentDistribution(departmentDistribution);
        
        return statsDTO;
    }

    /**
     * Tạo công ty mới
     */
    @Transactional
    public MessageResponse createCompany(CompanyDTO companyDTO, Long userId) {
        // Kiểm tra tên công ty đã tồn tại
        if (companyRepository.existsByName(companyDTO.getName())) {
            return new MessageResponse("Tên công ty đã tồn tại", false);
        }

        // Kiểm tra mã số thuế đã tồn tại
        if (companyDTO.getTaxCode() != null && !companyDTO.getTaxCode().isEmpty() && 
            companyRepository.existsByTaxCode(companyDTO.getTaxCode())) {
            return new MessageResponse("Mã số thuế đã tồn tại", false);
        }

        // Kiểm tra mã số doanh nghiệp đã tồn tại
        if (companyDTO.getBusinessCode() != null && !companyDTO.getBusinessCode().isEmpty() && 
            companyRepository.existsByBusinessCode(companyDTO.getBusinessCode())) {
            return new MessageResponse("Mã số doanh nghiệp đã tồn tại", false);
        }

        // Tạo công ty mới
        Company company = new Company();
        company.setName(companyDTO.getName());
        company.setLogo(companyDTO.getLogo());
        company.setEmail(companyDTO.getEmail());
        company.setPhone(companyDTO.getPhone());
        company.setAddress(companyDTO.getAddress());
        company.setWebsite(companyDTO.getWebsite());
        company.setTaxCode(companyDTO.getTaxCode());
        company.setBusinessCode(companyDTO.getBusinessCode());
        company.setEstablishedDate(companyDTO.getEstablishedDate());
        company.setActive(companyDTO.isActive());
        company.setCreatedAt(LocalDateTime.now());
        company.setCreatedBy(userId);

        company = companyRepository.save(company);

        // Tạo phòng ban mặc định
        createDefaultDepartments(company, userId);

        return new MessageResponse("Công ty đã được tạo thành công", true);
    }

    /**
     * Cập nhật công ty
     */
    @Transactional
    public MessageResponse updateCompany(Long id, CompanyDTO companyDTO, Long userId) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", id));

        // Kiểm tra tên công ty đã tồn tại
        if (!company.getName().equals(companyDTO.getName()) && 
            companyRepository.existsByName(companyDTO.getName())) {
            return new MessageResponse("Tên công ty đã tồn tại", false);
        }

        // Kiểm tra mã số thuế đã tồn tại
        if (companyDTO.getTaxCode() != null && !companyDTO.getTaxCode().isEmpty() && 
            !companyDTO.getTaxCode().equals(company.getTaxCode()) &&
            companyRepository.existsByTaxCode(companyDTO.getTaxCode())) {
            return new MessageResponse("Mã số thuế đã tồn tại", false);
        }

        // Kiểm tra mã số doanh nghiệp đã tồn tại
        if (companyDTO.getBusinessCode() != null && !companyDTO.getBusinessCode().isEmpty() && 
            !companyDTO.getBusinessCode().equals(company.getBusinessCode()) &&
            companyRepository.existsByBusinessCode(companyDTO.getBusinessCode())) {
            return new MessageResponse("Mã số doanh nghiệp đã tồn tại", false);
        }

        // Cập nhật công ty
        company.setName(companyDTO.getName());
        company.setLogo(companyDTO.getLogo());
        company.setEmail(companyDTO.getEmail());
        company.setPhone(companyDTO.getPhone());
        company.setAddress(companyDTO.getAddress());
        company.setWebsite(companyDTO.getWebsite());
        company.setTaxCode(companyDTO.getTaxCode());
        company.setBusinessCode(companyDTO.getBusinessCode());
        company.setEstablishedDate(companyDTO.getEstablishedDate());
        company.setActive(companyDTO.isActive());
        company.setUpdatedAt(LocalDateTime.now());
        company.setUpdatedBy(userId);

        companyRepository.save(company);

        return new MessageResponse("Công ty đã được cập nhật thành công", true);
    }

    /**
     * Xóa công ty (soft delete)
     */
    @Transactional
    public MessageResponse deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", id));

        // Kiểm tra công ty có nhân viên
        List<Employee> employees = employeeRepository.findByCompanyId(id);
        if (!employees.isEmpty()) {
            return new MessageResponse("Không thể xóa công ty đang có nhân viên", false);
        }

        // Thực hiện soft delete bằng cách đặt active = false
        company.setActive(false);
        companyRepository.save(company);

        return new MessageResponse("Công ty đã được vô hiệu hóa thành công", true);
    }
    
    /**
     * Khôi phục công ty đã bị vô hiệu hóa
     */
    @Transactional
    public MessageResponse restoreCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", id));
        
        if (company.isActive()) {
            return new MessageResponse("Công ty đang hoạt động", false);
        }
        
        company.setActive(true);
        companyRepository.save(company);
        
        return new MessageResponse("Công ty đã được khôi phục thành công", true);
    }

    /**
     * Tạo các phòng ban mặc định cho công ty mới
     */
    private void createDefaultDepartments(Company company, Long userId) {
        // Phòng Điều hành (Ban giám đốc)
        Department management = new Department();
        management.setName("Ban Giám đốc");
        management.setDescription("Ban lãnh đạo công ty");
        management.setCode("BGD");
        management.setCompany(company);
        management.setCreatedAt(LocalDateTime.now());
        management.setCreatedBy(userId);
        departmentRepository.save(management);
        
        // Phòng Nhân sự
        Department hr = new Department();
        hr.setName("Phòng Nhân sự");
        hr.setDescription("Quản lý nguồn nhân lực");
        hr.setCode("HR");
        hr.setCompany(company);
        hr.setCreatedAt(LocalDateTime.now());
        hr.setCreatedBy(userId);
        departmentRepository.save(hr);
        
        // Phòng Tài chính - Kế toán
        Department finance = new Department();
        finance.setName("Phòng Tài chính - Kế toán");
        finance.setDescription("Quản lý tài chính và kế toán");
        finance.setCode("TCKT");
        finance.setCompany(company);
        finance.setCreatedAt(LocalDateTime.now());
        finance.setCreatedBy(userId);
        departmentRepository.save(finance);
        
        // Phòng Kinh doanh
        Department sales = new Department();
        sales.setName("Phòng Kinh doanh");
        sales.setDescription("Quản lý hoạt động kinh doanh và bán hàng");
        sales.setCode("KD");
        sales.setCompany(company);
        sales.setCreatedAt(LocalDateTime.now());
        sales.setCreatedBy(userId);
        departmentRepository.save(sales);
        
        // Phòng IT
        Department it = new Department();
        it.setName("Phòng Công nghệ thông tin");
        it.setDescription("Quản lý hệ thống công nghệ thông tin");
        it.setCode("IT");
        it.setCompany(company);
        it.setCreatedAt(LocalDateTime.now());
        it.setCreatedBy(userId);
        departmentRepository.save(it);
    }

    /**
     * Chuyển đổi Company sang CompanyDTO
     */
    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setLogo(company.getLogo());
        dto.setEmail(company.getEmail());
        dto.setPhone(company.getPhone());
        dto.setAddress(company.getAddress());
        dto.setWebsite(company.getWebsite());
        dto.setTaxCode(company.getTaxCode());
        dto.setBusinessCode(company.getBusinessCode());
        dto.setEstablishedDate(company.getEstablishedDate());
        dto.setActive(company.isActive());
        return dto;
    }

    /**
     * Chuyển đổi CompanyDTO sang Company
     */
    private Company convertToEntity(CompanyDTO dto) {
        Company company = new Company();
        company.setId(dto.getId());
        company.setName(dto.getName());
        company.setLogo(dto.getLogo());
        company.setEmail(dto.getEmail());
        company.setPhone(dto.getPhone());
        company.setAddress(dto.getAddress());
        company.setWebsite(dto.getWebsite());
        company.setTaxCode(dto.getTaxCode());
        company.setBusinessCode(dto.getBusinessCode());
        company.setEstablishedDate(dto.getEstablishedDate());
        company.setActive(dto.isActive());
        return company;
    }
}