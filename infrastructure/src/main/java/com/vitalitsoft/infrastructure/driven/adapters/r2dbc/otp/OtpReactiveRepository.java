package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.otp;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OtpReactiveRepository extends ReactiveCrudRepository<OtpEntity, UUID>, ReactiveQueryByExampleExecutor<OtpEntity> {
        Mono<OtpEntity> findByCode(String code);
}
