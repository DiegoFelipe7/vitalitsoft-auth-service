package com.vitalitsoft.domain.refreshtoken;

import com.vitalitsoft.domain.shared.exception.VitalitSoftException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@With
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RefreshTokenModel {
    private UUID id;
    private UUID userId;
    private String email;
    private String token;
    private Boolean revoked;
    private LocalDateTime expirationTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void ensureValid() {
        if (this.revoked) {
            throw new VitalitSoftException(VitalitSoftException.Type.REFRESH_INVALID_TOKEN);
        }

        if (this.expirationTime.isBefore(LocalDateTime.now())) {
            throw new VitalitSoftException(VitalitSoftException.Type.REFRESH_TOKEN_EXPIRED);
        }
    }
}
