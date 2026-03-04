package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.auth;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AuthReactiveRepository extends ReactiveCrudRepository<AuthEntity, UUID>, ReactiveQueryByExampleExecutor<AuthEntity> {
    Mono<AuthEntity> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
}
