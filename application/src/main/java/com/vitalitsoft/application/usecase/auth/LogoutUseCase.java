package com.vitalitsoft.application.usecase.auth;

import com.vitalitsoft.application.dto.auth.response.LogoutResponse;
import com.vitalitsoft.application.mapper.auth.AuthResponseMapper;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class LogoutUseCase implements Function<String, Mono<LogoutResponse>> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRepository jwtRepository;

    @Override
    public Mono<LogoutResponse> apply(String token) {
        log.info("Iniciando proceso de logout");
        
        return jwtRepository.getSubject(token)
                .flatMap(userId -> refreshTokenRepository.revokeTokenByUserId(UUID.fromString(userId)))
                .then(Mono.fromCallable(AuthResponseMapper::toLogoutResponse))
                .doOnSuccess(response -> log.info("Logout exitoso: {}", response.getMessage()))
                .doOnError(error -> log.error("Error durante logout: {}", error.getMessage()));
    }
}
