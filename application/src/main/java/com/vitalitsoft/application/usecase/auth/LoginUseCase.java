package com.vitalitsoft.application.usecase.auth;


import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.auth.gateways.PasswordRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
public class LoginUseCase implements BiFunction<String, String, Mono<TokenModel>> {

    private final AuthRepository authRepository;
    private final PasswordRepository passwordRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRepository jwtRepository;

    @Override
    public Mono<TokenModel> apply(String email, String password) {
        log.info("Iniciando proceso de login para usuario: {}", email);

        return authRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new NexusException(
                        NexusException.Type.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                )))
                .flatMap(this::validateAuthStatus)
                .flatMap(user -> passwordRepository.matchPassword(password, user.getPassword())
                        .flatMap(match -> {
                            if (!match) {
                                log.warn("Credenciales inválidas para usuario: {}", email);
                                return Mono.error(new NexusException(
                                        NexusException.Type.INVALID_PASSWORD,
                                        HttpStatus.UNAUTHORIZED
                                ));
                            }
                            return Mono.just(user);
                        })
                )
                .flatMap(user -> refreshTokenRepository.revokeByEmail(email)
                        .then(jwtRepository.generateToken(user.getId().toString(), user.getRole().name()))
                        .flatMap(tokens ->
                                refreshTokenRepository
                                        .saveRefreshToken(email, user.getId(), tokens.getRefreshToken())
                                        .thenReturn(tokens)))
                .doOnSuccess(token -> log.info("Login exitoso para usuario: {}", email))
                .doOnError(error -> log.error("Error en login para usuario {}: {}", email, error.getMessage()));
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
}

