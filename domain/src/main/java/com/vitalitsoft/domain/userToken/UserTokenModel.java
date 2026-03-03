package com.vitalitsoft.domain.userToken;

import com.vitalitsoft.domain.shared.enums.TokenType;
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

    public boolean isValid() {
        return Boolean.FALSE.equals(this.used) && this.expirationTime.isAfter(LocalDateTime.now());
    }
}
