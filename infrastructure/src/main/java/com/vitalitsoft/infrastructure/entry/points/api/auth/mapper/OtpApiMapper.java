package com.vitalitsoft.infrastructure.entry.points.api.auth.mapper;

import com.vitalitsoft.application.command.otp.ResendOtpCommand;
import com.vitalitsoft.application.command.otp.VerifyOtpCommand;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.otp.request.ResendOtpRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.otp.request.ValidateOtpRequest;
import org.springframework.stereotype.Component;

public class OtpApiMapper {
    public static ResendOtpCommand toCommand(ResendOtpRequest dto) {
        return ResendOtpCommand.builder()
                .sessionId(dto.getSessionId())
                .build();
    }

    public static VerifyOtpCommand toCommand(ValidateOtpRequest dto) {
        return VerifyOtpCommand.builder()
                .sessionId(dto.getSessionId())
                .otp(dto.getOtp())
                .inactiveTwoFactor(dto.isInactiveTwoFactor())
                .build();
    }
}
