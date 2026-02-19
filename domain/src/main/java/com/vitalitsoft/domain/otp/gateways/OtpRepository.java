package com.vitalitsoft.domain.otp.gateways;

import com.vitalitsoft.domain.otp.OtpModel;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OtpRepository {
    Mono<OtpModel> generateOtp(UUID userId);
    Mono<OtpModel> getOtp(String code);
    Mono<Void> checkOtp(String code);
}
