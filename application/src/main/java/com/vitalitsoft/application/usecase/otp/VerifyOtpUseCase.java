package com.vitalitsoft.application.usecase.otp;

import com.vitalitsoft.application.dto.auth.response.LoginResponse;
import com.vitalitsoft.application.dto.otp.request.ValidateOtpRequest;
import com.vitalitsoft.application.mapper.auth.AuthMapper;
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
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class VerifyOtpUseCase implements Function<ValidateOtpRequest, Mono<LoginResponse>> {
    private static final int MAX_ATTEMPTS = 3;
    private final OtpRepository otpRepository;
    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;
    private final JwtRepository jwtRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public Mono<LoginResponse> apply(ValidateOtpRequest request) {
        log.info("Iniciando verificación de OTP para sessionId: {}", request.getSessionId());
        
        return otpRepository.findBySessionId(request.getSessionId())
                .flatMap(this::validateOtpState)
                .delayElement(Duration.ofSeconds(3))
                .flatMap(data -> verifyOtpCode(data, request.getOtp()))
                .flatMap(this::markOtpAsUsed)
                .flatMap(otpModel -> generateTokens(otpModel.getUserId(), request.isInactiveTwoFactor()))
                .map(AuthMapper::toLoginResponse)
                .doOnSuccess(response -> log.info("OTP verificado exitosamente"))
                .doOnError(error -> log.error("Error al verificar OTP: {}", error.getMessage()));
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
        log.debug("Verificando código OTP para sessionId: {}", otp.getSessionId());
        
        return hashingRepository.matches(rawOtp, otp.getCode())
                .flatMap(match -> {
                    if (!match) {
                        log.warn("OTP inválido para sessionId: {}", otp.getSessionId());
                        return incrementAttempts(otp)
                                .then(Mono.error(new NexusException(
                                        NexusException.Type.OTP_INVALID,
                                        HttpStatus.BAD_REQUEST
                                )));
                    }
                    log.debug("OTP válido para sessionId: {}", otp.getSessionId());
                    return Mono.just(otp);
                });
    }

    private Mono<Void> incrementAttempts(OtpModel otp) {
        OtpModel updatedOtp = otp.incrementAttempts();
        return otpRepository.save(updatedOtp).then();
    }

    private Mono<OtpModel> markOtpAsUsed(OtpModel otp) {
        log.debug("Marcando OTP como usado para sessionId: {}", otp.getSessionId());
        OtpModel usedOtp = otp.markAsUsed();
        return otpRepository.save(usedOtp);
    }


    private Mono<TokenModel> generateTokens(UUID userId, boolean disableTwoFactor) {
        log.debug("Generando tokens para userId: {}", userId);
        
        return authRepository.findById(userId)
                .flatMap(user -> updateTwoFactorIfNeeded(user, disableTwoFactor))
                .flatMap(this::issueTokens);
    }

    private Mono<AuthModel> updateTwoFactorIfNeeded(AuthModel authModel, boolean disableTwoFactor) {

        if (!disableTwoFactor) {
            return Mono.just(authModel);
        }

        log.debug("Deshabilitando 2FA temporalmente para: {}", authModel.getEmail());
        AuthModel updatedModel = authModel.withTwoFactorNotRequiredUntil(LocalDate.now().plusDays(7));

        return authRepository.save(updatedModel);
    }

    private Mono<TokenModel> issueTokens(AuthModel user) {
        log.debug("Emitiendo tokens para: {}", user.getEmail());
        
        return jwtRepository.generateToken(
                        user.getEmail(),
                        user.getRole().name(),
                        user.requiresTwoFactor()
                )
                .flatMap(tokens ->
                        refreshTokenRepository
                                .rotate(
                                        user.getEmail(),
                                        user.getId(),
                                        tokens.getRefreshToken()
                                )
                                .thenReturn(tokens)
                );
    }


}