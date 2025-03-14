package com.app.userservice.repository;

import com.app.userservice.entity.organization.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
    List<Company> findByActiveTrue();
    boolean existsByName(String name);
    boolean existsByTaxCode(String taxCode);
    boolean existsByBusinessCode(String businessCode);
}