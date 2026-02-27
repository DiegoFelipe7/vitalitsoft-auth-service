package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.auth.mapper;


import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.shared.enums.Role;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.auth.AuthEntity;

import java.time.LocalDateTime;

public class AuthMapper {

    private AuthMapper() {
        throw new IllegalStateException("Utility class");
    }


    public static AuthModel mapToModel(AuthEntity authEntity) {
        return AuthModel.builder()
                .id(authEntity.getId())
                .email(authEntity.getEmail())
                .password(authEntity.getPassword())
                .role(authEntity.getRole())
                .status(authEntity.getStatus())
                .twoFactorNotRequiredUntil(authEntity.getTwoFactorNotRequiredUntil())
                .updatedAt(authEntity.getUpdatedAt())
                .createdAt(authEntity.getCreatedAt())
                .build();
    }

    public static AuthModel toModel(AuthEntity authEntity) {
        return AuthModel.builder()
                .id(authEntity.getId())
                .email(authEntity.getEmail())
                .role(authEntity.getRole())
                .status(authEntity.getStatus())
                .updatedAt(authEntity.getUpdatedAt())
                .createdAt(authEntity.getCreatedAt())
                .build();
    }

    public static AuthEntity mapToEntity(AuthModel authModel) {
        return AuthEntity.builder()
                .email(authModel.getEmail())
                .password(authModel.getPassword())
                .role(Role.ADMIN)
                .status(Status.PENDING_VERIFICATION)
                .twoFactorNotRequiredUntil(authModel.getTwoFactorNotRequiredUntil())
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

}
