package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken;

import com.vitalitsoft.domain.shared.enums.TokenType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_token")
public class UserToken {
    @Id
    private UUID id;
    private UUID userId;
    private String email;
    private TokenType tokenType;
    private String token;
    private Boolean used;
    private LocalDateTime expirationTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
