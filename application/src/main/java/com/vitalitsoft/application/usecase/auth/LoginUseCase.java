package com.vitalitsoft.application.usecase.auth;


import com.vitalitsoft.application.dto.auth.request.LoginRequest;
import com.vitalitsoft.application.dto.auth.response.LoginResponse;
import com.vitalitsoft.application.mapper.auth.AuthMapper;
import com.vitalitsoft.application.mapper.otp.OtpMapper;
import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.SendOtpEventModel;
import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.domain.shared.utils.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
public class LoginUseCase implements Function<LoginRequest, Mono<LoginResponse>> {

    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRepository jwtRepository;
    private final OtpRepository otpRepository;
    private final EventsRepository<SendOtpEventModel> eventPublisher;

    @Override
    public Mono<LoginResponse> apply(LoginRequest request) {
        log.info("Iniciando proceso de login para usuario: {}", request.getEmail());

        return authRepository.findByEmail(request.getEmail())
                .doOnNext(AuthModel::ensureCanLogin)
                .flatMap(user -> validatePassword(user, request.getPassword()))
                .flatMap(this::processLoginFlow)
                .map(AuthMapper::toLoginResponse)
                .doOnSuccess(token -> log.info("Login successful for user: {}", request.getEmail()))
                .doOnError(error -> log.warn("Login failed for user {}: {}", request.getEmail(), error.getMessage()));
    }


    private Mono<AuthModel> validatePassword(AuthModel user, String rawPassword) {

        return hashingRepository.matches(rawPassword, user.getPassword())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.INVALID_PASSWORD, HttpStatus.UNAUTHORIZED)))
                .thenReturn(user);
    }

    private Mono<TokenModel> processLoginFlow(AuthModel user) {

        return user.requiresTwoFactor()
                ? initiateTwoFactor(user)
                : issueSession(user);
    }

    private Mono<TokenModel> initiateTwoFactor(AuthModel user) {

        String rawOtp = OtpGenerator.generateNumericOtp();

        return hashingRepository.hash(rawOtp)
                .map(hash -> OtpMapper.toModel(user.getId(), hash))
                .flatMap(otpRepository::save)
                .then(publishOtpEvent(user, rawOtp))
                .thenReturn(TokenModel.builder().isTwoFactorAuthRequired(true).build())
                .doOnSuccess(t -> log.info("2FA initiated for {}", user.getEmail()));
    }

    private Mono<Void> publishOtpEvent(AuthModel user, String rawOtp) {

        return eventPublisher.publish(
                "auth.exchange",
                "auth.otp.send",
                SendOtpEventModel.builder()
                        .email(user.getEmail())
                        .otp(rawOtp)
                        .build()
        );
    }


    private Mono<TokenModel> issueSession(AuthModel user) {
        return jwtRepository.generateToken(user.getEmail(), user.getRole().name(), user.requiresTwoFactor())
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

