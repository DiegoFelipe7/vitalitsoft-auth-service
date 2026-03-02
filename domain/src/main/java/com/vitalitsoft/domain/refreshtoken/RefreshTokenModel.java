package com.vitalitsoft.domain.refreshtoken;

import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
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
            throw new NexusException(
                    NexusException.Type.REFRESH_INVALID_TOKEN,
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (this.expirationTime.isBefore(LocalDateTime.now())) {
            throw new NexusException(
                    NexusException.Type.REFRESH_TOKEN_EXPIRED,
                    HttpStatus.UNAUTHORIZED
            );
        }
    }
}
