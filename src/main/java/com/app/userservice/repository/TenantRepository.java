package com.app.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.userservice.entity.tenant.Tenant;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);
    Optional<Tenant> findBySchema(String schema);
    Optional<Tenant> findByAdminUserId(Long adminUserId);
    boolean existsByName(String name);
    boolean existsBySchema(String schema);
}