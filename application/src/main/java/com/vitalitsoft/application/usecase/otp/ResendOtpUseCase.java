package com.vitalitsoft.application.usecase.otp;

import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.SendOtpEventModel;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.otp.OtpModel;
import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.domain.shared.utils.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ResendOtpUseCase implements Function<String, Mono<Void>> {

    private static final int MAX_RESEND = 2;
    private final AuthRepository authRepository;
    private final OtpRepository otpRepository;
    private final EventsRepository<SendOtpEventModel> eventPublisher;
    private final HashingRepository encryptionRepository;

    @Override
    public Mono<Void> apply(String sessionId) {
        return otpRepository.findBySessionId(sessionId)
                .flatMap(this::validateResendRules)
                .flatMap(this::processOtpRegeneration)
                .then();
    }

    private Mono<OtpModel> validateResendRules(OtpModel otp) {
        if (otp.isUsed()) {
            return Mono.error(new NexusException(NexusException.Type.OTP_ALREADY_USED, HttpStatus.BAD_REQUEST));
        }
        if (otp.canRetry(MAX_RESEND)) {
            return Mono.error(new NexusException(NexusException.Type.OTP_MAX_RESEND_ATTEMPTS, HttpStatus.BAD_REQUEST));
        }
        return Mono.just(otp);
    }


    private Mono<Void> processOtpRegeneration(OtpModel otp) {

        String rawOtp = OtpGenerator.generateNumericOtp();

        return encryptionRepository.hash(rawOtp)
                .map(hashedOtp -> {
                    otp.setCode(hashedOtp);
                    otp.incrementAttempts();
                    otp.setUpdatedAt(LocalDateTime.now());
                    return otp;
                })
                .flatMap(otpRepository::save)
                .flatMap(savedOtp -> publishOtpEvent(savedOtp, rawOtp));
    }

    private Mono<Void> publishOtpEvent(OtpModel otp, String rawOtp) {

        return authRepository.findById(otp.getUserId())
                .flatMap(user -> {

                    SendOtpEventModel event = SendOtpEventModel.builder()
                            .email(user.getEmail())
                            .otp(rawOtp)
                            .build();

                    return eventPublisher.publish(
                            "auth.exchange",
                            "auth.otp.send",
                            event
                    );
                })
                .doOnSuccess(unused -> log.info("Evento OTP publicado para userId={}", otp.getUserId()))
                .doOnError(error -> log.warn("Error publicando evento OTP para userId={}, error={}", otp.getUserId(), error.getMessage()));
    }

}
