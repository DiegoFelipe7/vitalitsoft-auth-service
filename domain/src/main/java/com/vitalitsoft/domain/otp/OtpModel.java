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
    private String code;
    private Instant expiresAt;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public String getMaskedCode() {

        int unmaskedLength = 4;
        String maskedPart = "*".repeat(code.length() - unmaskedLength);
        String unmaskedPart = code.substring(code.length() - unmaskedLength);
        return maskedPart + unmaskedPart;
    }

    public void markAsUsed() {
        this.used = true;
    }
}
