package com.vitalitsoft.application.usecase.auth;

import com.vitalitsoft.application.dto.auth.request.LogoutRequest;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class LogoutUseCase implements Function<LogoutRequest, Mono<Void>> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRepository jwtRepository;

    @Override
    public Mono<Void> apply(LogoutRequest request) {
        log.info("Iniciando proceso de logout");
        
        return jwtRepository.getSubject(request.getToken())
                .flatMap(userId -> refreshTokenRepository.revokeTokenByUserId(UUID.fromString(userId)))
                .doOnSuccess(response -> log.info("Logout exitoso: {}", ""))
                .doOnError(error -> log.error("Error durante logout: {}", error.getMessage()));
    }
}
