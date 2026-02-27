package com.vitalitsoft.application.usecase.auth;


import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.SendOtpEventModel;
import com.vitalitsoft.domain.otp.OtpModel;
import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.domain.shared.utils.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;


@Slf4j
@RequiredArgsConstructor
public class LoginUseCase implements BiFunction<String, String, Mono<TokenModel>> {

    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRepository jwtRepository;
    private final OtpRepository otpRepository;
    private final EventsRepository<SendOtpEventModel> eventPublisher;

    @Override
    public Mono<TokenModel> apply(String email, String password) {
        log.info("Iniciando proceso de login para usuario: {}", email);

        return findUser(email)
                .flatMap(this::validateAuthStatus)
                .flatMap(user -> validatePassword(user, password))
                .flatMap(this::handleTwoFactorIfRequired)
                .doOnSuccess(token -> log.info("Login successful for user: {}", email))
                .doOnError(error -> log.warn("Login failed for user {}: {}", email, error.getMessage()));
    }

    private Mono<AuthModel> findUser(String email) {
        return authRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(
                        new NexusException(
                                NexusException.Type.USER_NOT_FOUND,
                                HttpStatus.NOT_FOUND
                        )
                ));
    }


    private Mono<AuthModel> validateAuthStatus(AuthModel auth) {
        return switch (auth.getStatus()) {
            case INACTIVE -> {
                log.warn("Usuario inactivo: {}", auth.getEmail());
                yield Mono.error(new NexusException(
                        NexusException.Type.ACCOUNT_LOCKED,
                        HttpStatus.FORBIDDEN
                ));
            }
            case PENDING_VERIFICATION -> {
                log.warn("Usuario pendiente de verificación: {}", auth.getEmail());
                yield Mono.error(new NexusException(
                        NexusException.Type.PENDING_VERIFICATION,
                        HttpStatus.FORBIDDEN
                ));
            }
            case ACTIVE -> Mono.just(auth);
        };
    }

    private Mono<AuthModel> validatePassword(AuthModel user, String rawPassword) {
        return hashingRepository
                .matches(rawPassword, user.getPassword())
                .flatMap(match -> {
                    if (!match) {
                        return Mono.error(
                                new NexusException(
                                        NexusException.Type.INVALID_PASSWORD,
                                        HttpStatus.UNAUTHORIZED
                                )
                        );
                    }
                    return Mono.just(user);
                });
    }

    private Mono<TokenModel> handleTwoFactorIfRequired(AuthModel user) {
        if (user.requiresTwoFactor()) {
            log.info("Usuario con 2FA habilitado, iniciando proceso de verificación adicional para: {}", user.getEmail());
            return generateOtp(user);
        }
        return issueTokens(user);
    }

    private Mono<TokenModel> generateOtp(AuthModel user) {
        String otp = OtpGenerator.generateNumericOtp();
        return hashingRepository.hash(otp)
                .flatMap(hash -> {
                    OtpModel otpModel = OtpModel.builder()
                            .userId(user.getId())
                            .code(hash)
                            .build();
                    return otpRepository.save(otpModel);
                })
                .flatMap(savedOtp ->
                        eventPublisher.publish(
                                "auth.exchange",
                                "auth.otp.send",
                                SendOtpEventModel.builder()
                                        .email(user.getEmail())
                                        .otp(otp)
                                        .build()
                        )
                )
                .thenReturn(
                        TokenModel.builder()
                                .isTwoFactorAuthRequired(true)
                                .build()
                )
                .doOnSuccess(t -> log.info("OTP generated for user {}", user.getEmail()))
                .doOnError(e -> log.error("Error generating OTP for user {}", user.getEmail(), e));


    }


    private Mono<TokenModel> issueTokens(AuthModel user) {

        return refreshTokenRepository
                .revokeByEmail(user.getEmail())
                .then(jwtRepository.generateToken(user.getId().toString(), user.getRole().name(), user.requiresTwoFactor()))
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

