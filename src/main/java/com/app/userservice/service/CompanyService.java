package com.app.userservice.service;

import com.app.userservice.dto.CompanyDTO;
import com.app.userservice.dto.MessageResponse;
import com.app.userservice.entity.organization.Company;
import com.app.userservice.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Get all companies
     */
    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active companies
     */
    public List<CompanyDTO> getAllActiveCompanies() {
        return companyRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get company by ID
     */
    public CompanyDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return convertToDTO(company);
    }

    /**
     * Create a new company
     */
    @Transactional
    public MessageResponse createCompany(CompanyDTO companyDTO, Long userId) {
        // Check if company name exists
        if (companyRepository.existsByName(companyDTO.getName())) {
            return new MessageResponse("Company name already exists", false);
        }

        // Check if tax code exists
        if (companyDTO.getTaxCode() != null && !companyDTO.getTaxCode().isEmpty() && 
            companyRepository.existsByTaxCode(companyDTO.getTaxCode())) {
            return new MessageResponse("Tax code already exists", false);
        }

        // Check if business code exists
        if (companyDTO.getBusinessCode() != null && !companyDTO.getBusinessCode().isEmpty() && 
            companyRepository.existsByBusinessCode(companyDTO.getBusinessCode())) {
            return new MessageResponse("Business code already exists", false);
        }

        // Create new company
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

        companyRepository.save(company);

        return new MessageResponse("Company created successfully", true);
    }

    /**
     * Update an existing company
     */
    @Transactional
    public MessageResponse updateCompany(Long id, CompanyDTO companyDTO, Long userId) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Check if company name exists
        if (!company.getName().equals(companyDTO.getName()) && 
            companyRepository.existsByName(companyDTO.getName())) {
            return new MessageResponse("Company name already exists", false);
        }

        // Check if tax code exists
        if (companyDTO.getTaxCode() != null && !companyDTO.getTaxCode().isEmpty() && 
            !companyDTO.getTaxCode().equals(company.getTaxCode()) &&
            companyRepository.existsByTaxCode(companyDTO.getTaxCode())) {
            return new MessageResponse("Tax code already exists", false);
        }

        // Check if business code exists
        if (companyDTO.getBusinessCode() != null && !companyDTO.getBusinessCode().isEmpty() && 
            !companyDTO.getBusinessCode().equals(company.getBusinessCode()) &&
            companyRepository.existsByBusinessCode(companyDTO.getBusinessCode())) {
            return new MessageResponse("Business code already exists", false);
        }

        // Update company
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

        return new MessageResponse("Company updated successfully", true);
    }

    /**
     * Delete company by ID
     */
    @Transactional
    public MessageResponse deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            return new MessageResponse("Company not found", false);
        }

        // Perform soft delete by setting active to false
        Company company = companyRepository.findById(id).get();
        company.setActive(false);
        companyRepository.save(company);

        return new MessageResponse("Company deactivated successfully", true);
    }

    /**
     * Convert Company entity to DTO
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

    @SuppressWarnings("unused")
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