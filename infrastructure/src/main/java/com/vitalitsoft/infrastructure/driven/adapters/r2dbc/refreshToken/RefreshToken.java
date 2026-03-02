package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken {
    @Id
    private UUID id;
    private UUID userId;
    private String email;
    private String token;
    private Boolean revoked;
    private LocalDateTime expirationTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void revoke() {
        this.revoked = true;
        this.updatedAt = LocalDateTime.now();
    }
}
