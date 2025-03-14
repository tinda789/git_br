package com.app.userservice.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "device_info", length = 100)
    private String deviceInfo;

    @Column(name = "login_status")
    private boolean loginStatus;
    
    @Column(name = "login_method", length = 20)
    private String loginMethod; // PASSWORD, OAUTH2, 2FA

    @CreationTimestamp
    @Column(name = "login_time", updatable = false)
    private LocalDateTime loginTime;
    
    @Column(name = "logout_time")
    private LocalDateTime logoutTime;
    
    @Column(name = "session_duration")
    private Long sessionDuration;
    
    @Column(name = "failure_reason", length = 100)
    private String failureReason;
}