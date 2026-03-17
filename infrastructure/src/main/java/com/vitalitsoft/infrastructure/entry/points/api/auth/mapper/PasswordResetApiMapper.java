package com.vitalitsoft.infrastructure.entry.points.api.auth.mapper;

import com.vitalitsoft.application.command.passwordReset.ConfirmPasswordResetCommand;
import com.vitalitsoft.application.command.passwordReset.RequestResetPasswordCommand;
import com.vitalitsoft.application.command.passwordReset.ValidateTokenResetCommand;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.passwordReset.request.ConfirmPasswordResetRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.passwordReset.request.RequestResetPassword;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.passwordReset.request.ValidateTokenResetRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.passwordReset.response.ValidatePasswordResetTokenResponse;

public class PasswordResetApiMapper {
    public static RequestResetPasswordCommand toCommand(RequestResetPassword dto) {
        return RequestResetPasswordCommand.builder()
                .email(dto.getEmail())
                .build();
    }

    public static ValidateTokenResetCommand toCommand(ValidateTokenResetRequest dto) {
        return ValidateTokenResetCommand.builder()
                .token(dto.getToken())
                .tokenType(dto.getTokenType())
                .build();
    }

    public static ConfirmPasswordResetCommand toCommand(ConfirmPasswordResetRequest dto) {
        return ConfirmPasswordResetCommand.builder()
                .password(dto.getPassword())
                .token(dto.getToken())
                .tokenType(dto.getTokenType())
                .build();
    }

    public static ValidatePasswordResetTokenResponse toResponseDto(Boolean isValid) {
        return ValidatePasswordResetTokenResponse.builder()
                .isValid(isValid)
                .build();
    }
}
