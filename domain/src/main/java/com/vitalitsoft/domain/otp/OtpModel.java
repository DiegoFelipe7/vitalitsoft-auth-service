package co.com.bancolombia.security.mock.otp;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@With
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
    private int resendAttempts;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public OtpModel incrementAttempts() {
        return this.withAttempts(this.attempts + 1);
    }

    public OtpModel incrementResendAttempts() {
        return this.withResendAttempts(this.resendAttempts + 1);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean hasExceededVerificationAttempts(int maxAttempts) {
        return attempts >= maxAttempts;
    }

    public boolean hasExceededResendAttempts(int maxAttempts) {
        return resendAttempts >= maxAttempts;
    }

    public OtpModel markAsUsed() {
        return this.withUsed(true);
    }
}
