package com.app.userservice.entity.organization;

import com.app.userservice.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", length = 20)
    private String employeeId;  // Custom employee ID (e.g., EMP001)

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "termination_date")
    private LocalDate terminationDate;
    
    // Status: 1=active, 2=on leave, 3=terminated, etc.
    @Column(nullable = false)
    private int status = 1;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @OneToMany(mappedBy = "manager")
    private Set<Employee> subordinates = new HashSet<>();

    // Phòng ban chính
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    
    // Phòng ban phụ
    @ManyToMany
    @JoinTable(
        name = "employee_secondary_departments",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<Department> secondaryDepartments = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "employee_teams",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<Team> teams = new HashSet<>();

    // Vị trí chính
    @ManyToOne
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;
    
    @Column(length = 100)
    private String jobTitle;
    
    @Column(name = "work_email", length = 100)
    private String workEmail;
    
    @Column(name = "work_phone", length = 20)
    private String workPhone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
}