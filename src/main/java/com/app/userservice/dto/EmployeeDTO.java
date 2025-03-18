package com.app.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    
    @Size(max = 20, message = "Mã nhân viên không được vượt quá 20 ký tự")
    private String employeeId;
    
    @NotNull(message = "ID người dùng là bắt buộc")
    private Long userId;
    
    @NotNull(message = "ID công ty là bắt buộc")
    private Long companyId;
    
    @NotNull(message = "ID phòng ban chính là bắt buộc")
    private Long departmentId;
    
    @NotNull(message = "ID vị trí là bắt buộc")
    private Long positionId;
    
    private LocalDate hireDate;
    
    private LocalDate terminationDate;
    
    private int status = 1;
    
    private Long managerId;
    
    @Size(max = 100, message = "Chức danh không được vượt quá 100 ký tự")
    private String jobTitle;
    
    @Email(message = "Email công việc phải đúng định dạng")
    @Size(max = 100, message = "Email công việc không được vượt quá 100 ký tự")
    private String workEmail;
    
    @Size(max = 20, message = "Số điện thoại công việc không được vượt quá 20 ký tự")
    private String workPhone;
    
    private Set<Long> secondaryDepartmentIds = new HashSet<>();
    
    private Set<Long> teamIds = new HashSet<>();
}