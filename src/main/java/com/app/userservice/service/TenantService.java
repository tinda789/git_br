package com.app.userservice.service;

import com.app.userservice.config.TenantDataSourceConfig;
import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.TenantDTO;
import com.app.userservice.dto.TenantSignupRequest;
import com.app.userservice.entity.tenant.Tenant;
import com.app.userservice.entity.user.Role;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.RoleRepository;
import com.app.userservice.repository.TenantRepository;
import com.app.userservice.repository.UserRepository;
import com.app.userservice.tenant.TenantContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TenantDataSourceConfig tenantDataSourceConfig;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Value("${app.default-schema:public}")
    private String defaultSchema;

    /**
     * Get all tenants
     */
    public List<TenantDTO> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tenant by ID
     */
    public TenantDTO getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return convertToDTO(tenant);
    }

    /**
     * Create a new tenant
     */
    @Transactional
    public MessageResponse createTenant(TenantDTO tenantDTO) {
        // Check if tenant name exists
        if (tenantRepository.existsByName(tenantDTO.getName())) {
            return new MessageResponse("Tenant name already exists", false);
        }

        // Check if schema exists
        if (tenantRepository.existsBySchema(tenantDTO.getSchema())) {
            return new MessageResponse("Schema name already exists", false);
        }

        // Create tenant
        Tenant tenant = new Tenant();
        tenant.setName(tenantDTO.getName());
        tenant.setSchema(tenantDTO.getSchema());
        tenant.setDescription(tenantDTO.getDescription());
        tenant.setActive(tenantDTO.isActive());
        tenant.setCreatedAt(LocalDateTime.now());

        // Save tenant
        tenant = tenantRepository.save(tenant);
        
        // Create schema
        tenantDataSourceConfig.createSchema(tenant.getSchema());
        
        // Initialize schema tables
        initializeTenantSchema(tenant.getSchema());

        return new MessageResponse("Tenant created successfully", true);
    }

    /**
     * Tenant signup with admin user creation
     */
    @Transactional
    public MessageResponse tenantSignup(TenantSignupRequest request) {
        // Check if tenant name exists
        if (tenantRepository.existsByName(request.getCompanyName())) {
            return new MessageResponse("Company name already exists", false);
        }

        // Check if schema exists
        if (tenantRepository.existsBySchema(request.getSchema())) {
            return new MessageResponse("Schema name already exists", false);
        }
        
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new MessageResponse("Username is already taken", false);
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new MessageResponse("Email is already in use", false);
        }
        
        // Create tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getCompanyName());
        tenant.setSchema(request.getSchema());
        tenant.setDescription(request.getCompanyDescription());
        tenant.setActive(true);
        tenant.setCreatedAt(LocalDateTime.now());
        
        // Save tenant
        tenant = tenantRepository.save(tenant);
        
        // Create schema
        tenantDataSourceConfig.createSchema(tenant.getSchema());
        
        // Initialize schema tables
        initializeTenantSchema(tenant.getSchema());
        
        // Create admin user
        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFullName(request.getFullName());
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setStatus(1); // Active
        admin.setEmailVerified(true); // Auto-verify admin
        admin.setTenant(tenant);
        admin.setCreatedAt(LocalDateTime.now());
        
        // Assign ADMIN role
        Set<Role> roles = new HashSet<>();
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found"));
        roles.add(adminRole);
        
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found"));
        roles.add(userRole);
        
        admin.setRoles(roles);
        
        // Save admin user
        admin = userRepository.save(admin);
        
        // Update tenant with admin user ID
        tenant.setAdminUserId(admin.getId());
        tenantRepository.save(tenant);
        
        return new MessageResponse("Company registered successfully with admin account", true);
    }

    /**
     * Activate or deactivate a tenant
     */
    @Transactional
    public MessageResponse setTenantActiveStatus(Long id, boolean active) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        tenant.setActive(active);
        tenant.setUpdatedAt(LocalDateTime.now());
        
        tenantRepository.save(tenant);
        
        String status = active ? "activated" : "deactivated";
        return new MessageResponse("Tenant " + status + " successfully", true);
    }
    
    /**
     * Initialize schema for tenant with all necessary tables
     */
    @Transactional
    public void initializeTenantSchema(String schema) {
        // Set context to the new schema
        String originalSchema = TenantContext.getCurrentTenant();
        try {
            TenantContext.setCurrentTenant(schema);
            
            // Create companies table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS companies (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "logo VARCHAR(255), " +
                    "email VARCHAR(255), " +
                    "phone VARCHAR(50), " +
                    "address TEXT, " +
                    "website VARCHAR(255), " +
                    "tax_code VARCHAR(50), " +
                    "business_code VARCHAR(50), " +
                    "established_date DATE, " +
                    "active BOOLEAN DEFAULT true, " +
                    "created_at TIMESTAMP, " +
                    "updated_at TIMESTAMP, " +
                    "created_by BIGINT, " +
                    "updated_by BIGINT" +
                    ")");
            
            // Create departments table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS departments (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "code VARCHAR(50), " +
                    "description TEXT, " +
                    "company_id BIGINT NOT NULL, " +
                    "parent_id BIGINT, " +
                    "manager_id BIGINT, " +
                    "active BOOLEAN DEFAULT true, " +
                    "created_at TIMESTAMP, " +
                    "updated_at TIMESTAMP, " +
                    "created_by BIGINT, " +
                    "updated_by BIGINT, " +
                    "CONSTRAINT fk_department_company FOREIGN KEY (company_id) REFERENCES companies (id), " +
                    "CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES departments (id)" +
                    ")");
            
            // Thêm các bảng khác nếu cần thiết
            // ...
            
        } finally {
            // Restore original schema context
            TenantContext.setCurrentTenant(originalSchema);
        }
    }

    /**
     * Convert Tenant entity to DTO
     */
    private TenantDTO convertToDTO(Tenant tenant) {
        TenantDTO dto = new TenantDTO();
        dto.setId(tenant.getId());
        dto.setName(tenant.getName());
        dto.setSchema(tenant.getSchema());
        dto.setDescription(tenant.getDescription());
        dto.setActive(tenant.isActive());
        return dto;
    }
}