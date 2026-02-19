package com.vitalitsoft.application.usecase.otp;

import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RequiredArgsConstructor
public class ValidateOtpUseCase implements Function<String, Mono<Boolean>> {
    private final OtpRepository otpRepository;

    @Override
    public Mono<Boolean> apply(String command) {
        return Mono.just(true);
    }


}
