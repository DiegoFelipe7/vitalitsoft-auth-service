package com.vitalitsoft.application.usecase.otp;

import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.otp.OtpModel;
import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
public class VerifyOtpUseCase {
    private static final int MAX_ATTEMPTS = 3;
    private final OtpRepository otpRepository;
    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;
    private final JwtRepository jwtRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    public Mono<TokenModel> apply(String sessionId, String otp, boolean inactiveTwoFactor) {
        return otpRepository.findBySessionId(sessionId)
                .flatMap(this::validateOtpState)
                .flatMap(data -> verifyOtpCode(data, otp))
                .flatMap(this::markOtpAsUsed)
                .flatMap(ele -> generateTokens(ele.getUserId(), inactiveTwoFactor));
    }

    private Mono<OtpModel> validateOtpState(OtpModel otp) {
        if (otp.hasExceededVerificationAttempts(MAX_ATTEMPTS)) {
            return Mono.error(new NexusException(
                    NexusException.Type.OTP_MAX_ATTEMPTS,
                    HttpStatus.BAD_REQUEST
            ));
        }

        if (otp.isUsed()) {
            return Mono.error(new NexusException(
                    NexusException.Type.OTP_ALREADY_USED,
                    HttpStatus.BAD_REQUEST
            ));
        }

        if (otp.isExpired()) {
            return Mono.error(new NexusException(
                    NexusException.Type.OTP_EXPIRED,
                    HttpStatus.BAD_REQUEST
            ));
        }


        return Mono.just(otp);
    }

    private Mono<OtpModel> verifyOtpCode(OtpModel otp, String rawOtp) {

        return hashingRepository.matches(rawOtp, otp.getCode())
                .flatMap(match -> {
                    if (!match) {
                        return incrementAttempts(otp)
                                .then(Mono.error(new NexusException(
                                        NexusException.Type.OTP_INVALID,
                                        HttpStatus.BAD_REQUEST
                                )));
                    }
                    return Mono.just(otp);
                });
    }

    private Mono<Void> incrementAttempts(OtpModel otp) {
        otp.incrementAttempts();
        return otpRepository.save(otp).then();
    }

    private Mono<OtpModel> markOtpAsUsed(OtpModel otp) {
        otp.markAsUsed();
        return otpRepository.save(otp);
    }


    private Mono<TokenModel> generateTokens(UUID userId, boolean disableTwoFactor) {

        return authRepository.findById(userId)
                .flatMap(user -> updateTwoFactorIfNeeded(user, disableTwoFactor))
                .flatMap(this::issueTokens);
    }

    private Mono<AuthModel> updateTwoFactorIfNeeded(AuthModel authModel, boolean disableTwoFactor) {

        if (!disableTwoFactor) {
            return Mono.just(authModel);
        }

        authModel.setTwoFactorNotRequiredUntil(LocalDate.now().plusDays(7));

        return authRepository.save(authModel);
    }

    private Mono<TokenModel> issueTokens(AuthModel user) {

        return refreshTokenRepository
                .revokeByEmail(user.getEmail())
                .then(jwtRepository.generateToken(
                        user.getId().toString(),
                        user.getRole().name(),
                        user.requiresTwoFactor()
                ))
                .flatMap(tokens ->
                        refreshTokenRepository
                                .save(
                                        user.getEmail(),
                                        user.getId(),
                                        tokens.getRefreshToken()
                                )
                                .thenReturn(tokens)
                );
    }


}