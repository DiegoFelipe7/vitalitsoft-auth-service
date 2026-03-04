package com.vitalitsoft.application.mapper.userToken;

import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;

import java.time.LocalDateTime;
import java.util.UUID;

public final class UserTokenMapper {

    private UserTokenMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static UserTokenModel toPasswordModel(String email) {
        return UserTokenModel.builder()
                .email(email)
                .token(UUID.randomUUID().toString())
                .used(false)
                .tokenType(TokenType.PASSWORD_RESET)
                .expirationTime(LocalDateTime.now().plusHours(6))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserTokenModel toActivateModel(UUID userId, String email) {
        return UserTokenModel.builder()
                .email(email)
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .used(false)
                .tokenType(TokenType.ACTIVATE_ACCOUNT)
                .expirationTime(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
