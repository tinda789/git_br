package com.app.userservice.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_verification_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, unique = true, length = 100)
    private String token;
    
    @Column(name = "token_type", length = 20)
    private String tokenType; // EMAIL_VERIFICATION, PASSWORD_RESET, etc.
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "is_used")
    private boolean isUsed = false;
    
    @Column(name = "ip_used", length = 45)
    private String ipUsed;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}