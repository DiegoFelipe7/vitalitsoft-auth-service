package com.vitalitsoft.domain.otp.gateways;

import com.vitalitsoft.domain.otp.OtpModel;
import reactor.core.publisher.Mono;

public interface OtpRepository {
    Mono<OtpModel> save(OtpModel otpModel);
    Mono<OtpModel> findBySessionId(String sessionId);
}
