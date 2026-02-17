package com.vitalitsoft.application.usecase.auth;

import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RequiredArgsConstructor
public class LogoutUseCase implements Function<String, Mono<Void>> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRepository jwtRepository;

    @Override
    public Mono<Void> apply(String token) {
        return jwtRepository.getEmailFromToken(token)
                .flatMap(refreshTokenRepository::revokeByEmail)
                .then();
    }
}
