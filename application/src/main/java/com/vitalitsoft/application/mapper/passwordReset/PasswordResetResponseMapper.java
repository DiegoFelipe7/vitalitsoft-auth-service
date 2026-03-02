package com.vitalitsoft.application.mapper.passwordReset;

import com.vitalitsoft.application.dto.passwordReset.response.ConfirmPasswordResetResponse;
import com.vitalitsoft.application.dto.passwordReset.response.RequestResetPasswordResponse;
import com.vitalitsoft.application.dto.passwordReset.response.ValidatePasswordResetTokenResponse;

public class PasswordResetResponseMapper {

    private PasswordResetResponseMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static RequestResetPasswordResponse toRequestResetPasswordResponse(String email) {
        return RequestResetPasswordResponse.builder()
                .email(email)
                .message("Se ha enviado un correo electrónico con las instrucciones para restablecer tu contraseña")
                .build();
    }

    public static ValidatePasswordResetTokenResponse toValidatePasswordResetTokenResponse(Boolean isValid) {
        String message = isValid 
                ? "Token válido" 
                : "Token inválido o expirado";
        
        return ValidatePasswordResetTokenResponse.builder()
                .isValid(isValid)
                .message(message)
                .build();
    }

    public static ConfirmPasswordResetResponse toConfirmPasswordResetResponse(String email) {
        return ConfirmPasswordResetResponse.builder()
                .email(email)
                .message("Contraseña restablecida exitosamente")
                .build();
    }
}
