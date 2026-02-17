package com.vitalitsoft.domain.userToken;
import com.vitalitsoft.domain.shared.enums.TokenType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
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
}
