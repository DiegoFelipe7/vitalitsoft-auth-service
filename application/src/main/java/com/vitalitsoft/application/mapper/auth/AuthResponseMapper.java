package com.vitalitsoft.application.mapper.auth;

import com.vitalitsoft.application.dto.auth.response.*;
import com.vitalitsoft.domain.auth.TokenModel;

import java.util.UUID;

public class AuthResponseMapper {

    private AuthResponseMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static RegisterUserResponse toRegisterUserResponse(UUID userId, String email) {
        return RegisterUserResponse.builder()
                .userId(userId)
                .email(email)
                .message("Usuario registrado exitosamente. Por favor verifica tu correo electrónico.")
                .build();
    }

    public static LoginResponse toLoginResponse(TokenModel tokenModel) {
        return LoginResponse.builder()
                .token(tokenModel.getToken())
                .refreshToken(tokenModel.getRefreshToken())
                .isTwoFactorAuthRequired(tokenModel.getIsTwoFactorAuthRequired())
                .build();
    }

    public static LogoutResponse toLogoutResponse() {
        return LogoutResponse.builder()
                .message("Sesión cerrada exitosamente")
                .build();
    }

    public static RefreshSessionResponse toRefreshSessionResponse(TokenModel tokenModel) {
        return RefreshSessionResponse.builder()
                .token(tokenModel.getToken())
                .refreshToken(tokenModel.getRefreshToken())
                .isTwoFactorAuthRequired(tokenModel.getIsTwoFactorAuthRequired())
                .build();
    }

    public static ActivateAccountResponse toActivateAccountResponse(String email) {
        return ActivateAccountResponse.builder()
                .email(email)
                .message("Cuenta activada exitosamente")
                .build();
    }
}
