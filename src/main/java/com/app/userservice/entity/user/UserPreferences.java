package com.app.userservice.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "language", length = 10)
    private String language = "en";
    
    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";
    
    @Column(name = "theme", length = 20)
    private String theme = "light";
    
    @Column(name = "notification_email")
    private boolean notificationEmail = true;
    
    @Column(name = "notification_web")
    private boolean notificationWeb = true;
    
    @Column(name = "notification_mobile")
    private boolean notificationMobile = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}