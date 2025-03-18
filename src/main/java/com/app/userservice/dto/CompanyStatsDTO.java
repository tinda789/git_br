package com.app.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatsDTO {
    private String companyName;
    private long totalDepartments;
    private long totalEmployees;
    private Map<String, Double> departmentDistribution; // Phòng ban và phần trăm nhân viên
}