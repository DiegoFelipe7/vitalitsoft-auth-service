package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.otp;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("otp")
public class OtpEntity {
    @Id
    private UUID id;
    private UUID userId;
    private String sessionId;
    private String code;
    private Instant expiresAt;
    private int attempts;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
