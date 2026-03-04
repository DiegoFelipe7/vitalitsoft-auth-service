package com.vitalitsoft.domain.otp.gateways;

import com.vitalitsoft.domain.otp.OtpModel;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OtpRepository {
    Mono<OtpModel> save(OtpModel otpModel);
    Mono<OtpModel> findBySessionId(String sessionId);
    Mono<OtpModel> markAsUsed(UUID uuid);
    Mono<OtpModel> update(OtpModel model);
}
