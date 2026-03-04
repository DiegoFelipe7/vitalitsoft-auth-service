package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefreshTokenReactiveRepository extends ReactiveCrudRepository<RefreshToken, UUID>, ReactiveQueryByExampleExecutor<RefreshToken> {
    Flux<RefreshToken> findAllByUserId(UUID userId);
    Mono<RefreshToken> findByToken(String token);
}
