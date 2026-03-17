package com.vitalitsoft.infrastructure.entry.points.api.auth.mapper;

import com.vitalitsoft.application.command.auth.ActivateAccountCommand;
import com.vitalitsoft.application.command.auth.LoginCommand;
import com.vitalitsoft.application.command.auth.RegisterUserCommand;
import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.auth.request.ActivateAccountRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.auth.request.LoginRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.auth.request.RegisterUserRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.auth.response.LoginResponse;

public class AuthApiMapper {
    public static LoginCommand toCommand(LoginRequest dto) {
        return LoginCommand.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .build();
    }

    public static RegisterUserCommand toCommand(RegisterUserRequest dto) {
        return RegisterUserCommand.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .phoneNumber(dto.getPhoneNumber())
                .termsAccepted(dto.getTermsAccepted())
                .build();
    }

    public static ActivateAccountCommand toCommand(ActivateAccountRequest dto) {
        return ActivateAccountCommand.builder()
                .token(dto.getToken())
                .build();
    }

    public static LoginResponse toResponseDto(TokenModel tokenModel) {
        return LoginResponse.builder()
                .accessToken(tokenModel.getAccessToken())
                .refreshToken(tokenModel.getRefreshToken())
                .isTwoFactorAuthRequired(tokenModel.getIsTwoFactorAuthRequired())
                .sessionId(tokenModel.getSessionId())
                .build();
    }
}
