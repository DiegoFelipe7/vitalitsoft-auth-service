package com.vitalitsoft.domain.otp;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class OtpModel {
    private UUID id;
    private UUID userId;
    private String sessionId;
    private String code;
    private Instant expiresAt;
    private int attempts;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public void incrementAttempts() {
        this.attempts++;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean canRetry(int maxAttempts) {
        return attempts >= maxAttempts;
    }

    public void markAsUsed() {
        this.used = true;
    }
}
