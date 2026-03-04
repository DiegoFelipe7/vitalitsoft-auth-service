package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.otp.mapper;

import com.vitalitsoft.domain.otp.OtpModel;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.otp.OtpEntity;

public final class OtpMapper {

    private OtpMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static OtpEntity toEntity (OtpModel otpModel) {
        return OtpEntity
                .builder()
                .id(otpModel.getId())
                .userId(otpModel.getUserId())
                .sessionId(otpModel.getSessionId())
                .attempts(otpModel.getAttempts())
                .resendAttempts(otpModel.getResendAttempts())
                .code(otpModel.getCode())
                .expiresAt(otpModel.getExpiresAt())
                .used(otpModel.isUsed())
                .createdAt(otpModel.getCreatedAt())
                .updatedAt(otpModel.getUpdatedAt())
                .build();
    }

    public static OtpModel toModel(OtpEntity otpEntity) {
        return OtpModel
                .builder()
                .id(otpEntity.getId())
                .userId(otpEntity.getUserId())
                .sessionId(otpEntity.getSessionId())
                .attempts(otpEntity.getAttempts())
                .resendAttempts(otpEntity.getResendAttempts())
                .code(otpEntity.getCode())
                .expiresAt(otpEntity.getExpiresAt())
                .used(otpEntity.isUsed())
                .createdAt(otpEntity.getCreatedAt())
                .updatedAt(otpEntity.getUpdatedAt())
                .build();
    }
}
