package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken.mapper;


import com.vitalitsoft.domain.refreshtoken.RefreshTokenModel;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken.RefreshToken;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshTokenMapper {

    private RefreshTokenMapper() {
        throw new IllegalStateException("Utility class");
    }


    public static RefreshTokenModel mapToModel(RefreshToken refreshToken) {
        return RefreshTokenModel.builder()
                .id(refreshToken.getId())
                .userId(refreshToken.getUserId())
                .email(refreshToken.getEmail())
                .token(refreshToken.getToken())
                .revoked(refreshToken.getRevoked())
                .expirationTime(refreshToken.getExpirationTime())
                .updatedAt(refreshToken.getUpdatedAt())
                .createdAt(refreshToken.getCreatedAt())
                .build();
    }

    public static RefreshToken mapToEntity(RefreshTokenModel refreshTokenModel) {
        return RefreshToken.builder()
                .userId(refreshTokenModel.getUserId())
                .email(refreshTokenModel.getEmail())
                .token(refreshTokenModel.getToken())
                .revoked(refreshTokenModel.getRevoked())
                .expirationTime(refreshTokenModel.getExpirationTime())
                .updatedAt(refreshTokenModel.getUpdatedAt())
                .createdAt(refreshTokenModel.getCreatedAt())
                .build();
    }

    public static RefreshToken toEntity(UUID userId, String email, String token) {
        return RefreshToken.builder()
                .userId(userId)
                .email(email)
                .token(token)
                .revoked(false)
                .expirationTime(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
