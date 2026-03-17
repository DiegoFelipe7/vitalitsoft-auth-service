package com.vitalitsoft.domain.userToken;

import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.shared.exception.VitalitSoftException;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserTokenModel {
    private UUID id;
    private UUID userId;
    private TokenType tokenType;
    private String email;
    private String token;
    private Boolean used;
    private LocalDateTime expirationTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void verifyValidity() {

        if (Boolean.TRUE.equals(this.used)) {
            throw new VitalitSoftException(VitalitSoftException.Type.INVALID_TOKEN);
        }
        if (this.expirationTime.isBefore(LocalDateTime.now())) {
            throw new VitalitSoftException(VitalitSoftException.Type.TOKEN_EXPIRED);
        }
    }
}
