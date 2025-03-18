package com.app.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDetailDTO {
    private CompanyDTO company;
    private long departmentCount;
    private long employeeCount;
    private long activeEmployeeCount;
}