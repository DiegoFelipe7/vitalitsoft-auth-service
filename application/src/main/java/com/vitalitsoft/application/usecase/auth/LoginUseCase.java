package com.vitalitsoft.application.usecase.auth;


import com.vitalitsoft.application.command.auth.LoginCommand;
import com.vitalitsoft.application.mapper.auth.AuthMapper;
import com.vitalitsoft.application.mapper.otp.OtpMapper;
import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.SendOtpEventModel;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.domain.shared.constants.RabbitEvent;
import com.vitalitsoft.domain.shared.exception.VitalitSoftException;
import com.vitalitsoft.domain.shared.utils.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
public class LoginUseCase implements Function<LoginCommand, Mono<TokenModel>> {

    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRepository jwtRepository;
    private final OtpRepository otpRepository;
    private final EventsRepository<SendOtpEventModel> eventPublisher;

    @Override
    public Mono<TokenModel> apply(LoginCommand command) {
        log.info("Iniciando proceso de login para usuario: {}", command.getEmail());

        return authRepository.findByEmail(command.getEmail())
                .doOnNext(AuthModel::ensureCanLogin)
                .flatMap(user -> validatePassword(user, command.getPassword()))
                .flatMap(this::processLoginFlow)
                .doOnSuccess(token -> log.info("Login successful for user: {}", command.getEmail()))
                .doOnError(error -> log.warn("Login failed for user {}: {}", command.getEmail(), error.getMessage()));
    }


    private Mono<AuthModel> validatePassword(AuthModel user, String rawPassword) {

        return hashingRepository.matches(rawPassword, user.getPassword())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new VitalitSoftException(VitalitSoftException.Type.INVALID_PASSWORD)))
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
                .map(hash -> OtpMapper.toOtpModel(user.getId(), hash))
                .flatMap(otpRepository::save)
                .flatMap(otp -> publishOtpEvent(user.getEmail(), otp.getSessionId(), rawOtp).thenReturn(otp))
                .map(savedOtp -> AuthMapper.toTokenModel(savedOtp.getSessionId()))
                .doOnSuccess(t -> log.info("2FA initiated for {}", user.getEmail()));
    }

    private Mono<Void> publishOtpEvent(String email, String sessionId, String rawOtp) {

        SendOtpEventModel event = SendOtpEventModel.builder()
                .sessionId(sessionId)
                .email(email)
                .otp(rawOtp)
                .build();

        return eventPublisher.publish(RabbitEvent.GENERATE_OTP, event)
                .doOnSuccess(unused -> log.info("OTP publicado para el email {}", email))
                .doOnError(error -> log.error("Error publicando OTP para email {}: {}", email, error.getMessage()));
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

