package io.werescuecats.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_tokens")
@Data
@NoArgsConstructor
public class ApiToken {
    
    @Id
    private String token;
    
    @Column(nullable = false)
    private String organizationName;
    
    @Column(nullable = false)
    private String contactEmail;
    
    @Column(nullable = false)
    private Integer dailyRequestLimit;
    
    @Column(nullable = false)
    private Integer requestCount = 0;
    
    @Column(nullable = true)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = true)
    private LocalDateTime lastUsedAt;
    
    @Column(nullable = false)
    private boolean active = true;
    
    public ApiToken(String token, String organizationName) {
        this.token = token;
        this.organizationName = organizationName;
        this.contactEmail = organizationName.toLowerCase().replace(" ", ".") + "@example.com";
        this.dailyRequestLimit = 1000;
        this.requestCount = 0;
        this.description = "API token for " + organizationName;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusYears(1);
        this.lastUsedAt = null;
        this.active = true;
    }
    
    public boolean isValid() {
        return active && LocalDateTime.now().isBefore(expiresAt);
    }
    
    public boolean hasRequestsRemaining() {
        return requestCount < dailyRequestLimit;
    }
    
    public void incrementRequestCount() {
        this.requestCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
}