package com.vitalitsoft.application.mapper.otp;

import com.vitalitsoft.domain.otp.OtpModel;

import java.util.UUID;

public class OtpMapper {

     private OtpMapper() {
        throw new IllegalStateException("Utility class");
    }

     public static OtpModel toModel(UUID uuid, String code) {
        return OtpModel.builder()
                .userId(uuid)
                .code(code)
                .build();
    }
}
