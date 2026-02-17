package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken.mapper;


import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken.UserToken;

public final class UserTokenMapper {

    private UserTokenMapper() {
        throw new IllegalStateException("Utility class");
    }


    public static UserTokenModel mapToModel(UserToken userToken) {
        return UserTokenModel.builder()
                .id(userToken.getId())
                .userId(userToken.getUserId())
                .tokenType(userToken.getTokenType())
                .email(userToken.getEmail())
                .token(userToken.getToken())
                .used(userToken.getUsed())
                .expirationTime(userToken.getExpirationTime())
                .updatedAt(userToken.getUpdatedAt())
                .createdAt(userToken.getCreatedAt())
                .build();
    }

    public static UserToken mapToEntity(UserTokenModel userTokenModel) {
        return UserToken.builder()
                .userId(userTokenModel.getUserId())
                .email(userTokenModel.getEmail())
                .token(userTokenModel.getToken())
                .tokenType(userTokenModel.getTokenType())
                .used(userTokenModel.getUsed())
                .expirationTime(userTokenModel.getExpirationTime())
                .updatedAt(userTokenModel.getUpdatedAt())
                .createdAt(userTokenModel.getCreatedAt())
                .build();
    }

}
