package com.vitalitsoft.application.usecase.otp;

import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class SendOtpUseCase implements BiFunction<String, String, Mono<Void>> {
    private final OtpRepository otpRepository;

    @Override
    public Mono<Void> apply(String destination, String channel) {
        String otp = generateOtp();
        return Mono.empty();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
