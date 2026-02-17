package com.vitalitsoft.application.usecase.auth;


import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.refreshtoken.RefreshTokenModel;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.shared.exception.NexusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.Function;
@Slf4j
@RequiredArgsConstructor
public class RefreshSessionTokenUseCase
        implements Function<String, Mono<TokenModel>> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRepository jwtRepository;
    private final AuthRepository authRepository;

    @Override
    public Mono<TokenModel> apply(String refreshToken) {
        log.info("Iniciando proceso de refresh token");

        return refreshTokenRepository.findByToken(refreshToken)
                .switchIfEmpty(Mono.error(new NexusException(
                        NexusException.Type.TOKEN_NOT_FOUND,
                        HttpStatus.UNAUTHORIZED
                )))
                .flatMap(this::validateRefreshToken)
                .flatMap(entity ->
                        authRepository.findByEmail(entity.getEmail())
                                .switchIfEmpty(Mono.error(new NexusException(
                                        NexusException.Type.USER_NOT_FOUND,
                                        HttpStatus.NOT_FOUND
                                )))
                                .flatMap(user -> {
                                    if (user.getStatus() != Status.ACTIVE) {
                                        return Mono.error(new NexusException(
                                                NexusException.Type.ACCOUNT_LOCKED,
                                                HttpStatus.FORBIDDEN
                                        ));
                                    }

                                    return refreshAndGenerateTokens(user);
                                })
                )
                .doOnSuccess(t -> log.info("Refresh token exitoso"))
                .doOnError(e ->
                        log.error("Error en refresh token {}: {}", refreshToken, e.getMessage())
                );
    }

    private Mono<RefreshTokenModel> validateRefreshToken(RefreshTokenModel token) {
        if (Boolean.TRUE.equals(token.getRevoked())) {
            log.warn("Refresh token revocado");
            return Mono.error(new NexusException(
                    NexusException.Type.REFRESH_INVALID_TOKEN,
                    HttpStatus.UNAUTHORIZED
            ));
        }

        if (token.getExpirationTime().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token expirado");
            return Mono.error(new NexusException(
                    NexusException.Type.REFRESH_TOKEN_EXPIRED,
                    HttpStatus.UNAUTHORIZED
            ));
        }

        return Mono.just(token);
    }


    private Mono<TokenModel> refreshAndGenerateTokens(AuthModel authModel) {
        return refreshTokenRepository.revokeByEmail(authModel.getEmail())
                .then(jwtRepository.generateToken(
                        authModel.getEmail(),
                        authModel.getRole().name()
                ))
                .flatMap(tokens ->
                        refreshTokenRepository
                                .saveRefreshToken(
                                        authModel.getEmail(),
                                        authModel.getId(),
                                        tokens.getRefreshToken()
                                )
                                .thenReturn(tokens)
                );
    }
}
