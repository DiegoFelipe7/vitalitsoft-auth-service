package com.vitalitsoft.application.mapper.otp;

import com.vitalitsoft.domain.events.model.SendOtpEventModel;
import com.vitalitsoft.domain.otp.OtpModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class OtpMapper {

    private OtpMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static OtpModel toOtpModel(UUID uuid, String code) {
        return OtpModel.builder()
                .userId(uuid)
                .code(code)
                .sessionId(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusSeconds(300))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static SendOtpEventModel toSendOtpEventModel(String email, String sessionId, String rawOtp) {
        return SendOtpEventModel.builder()
                .sessionId(sessionId)
                .email(email)
                .otp(rawOtp)
                .build();
    }
}
