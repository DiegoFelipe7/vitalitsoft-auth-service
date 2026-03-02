package com.vitalitsoft.application.mapper.otp;

import com.vitalitsoft.application.dto.otp.response.ResendOtpResponse;
import com.vitalitsoft.application.dto.otp.response.VerifyOtpResponse;
import com.vitalitsoft.domain.auth.TokenModel;

public class OtpResponseMapper {

    private OtpResponseMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static VerifyOtpResponse toVerifyOtpResponse(TokenModel tokenModel) {
        return VerifyOtpResponse.builder()
                .token(tokenModel.getToken())
                .refreshToken(tokenModel.getRefreshToken())
                .isTwoFactorAuthRequired(tokenModel.getIsTwoFactorAuthRequired())
                .build();
    }

    public static ResendOtpResponse toResendOtpResponse() {
        return ResendOtpResponse.builder()
                .message("OTP reenviado exitosamente")
                .build();
    }
}
