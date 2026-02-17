package com.vitalitsoft.application.mapper.passwordReset;

import com.vitalitsoft.domain.userToken.UserTokenModel;

import java.time.LocalDateTime;
import java.util.UUID;

public final class PasswordResetMapper {

    private PasswordResetMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static UserTokenModel toPasswordResetModel(String email) {
        return UserTokenModel.builder()
                .email(email)
                .token(UUID.randomUUID().toString())
                .used(false)
                .expirationTime(LocalDateTime.now().plusHours(6))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
