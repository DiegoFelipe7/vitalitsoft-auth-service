package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken.mapper;


import com.vitalitsoft.domain.refreshtoken.RefreshTokenModel;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken.RefreshToken;

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

}
