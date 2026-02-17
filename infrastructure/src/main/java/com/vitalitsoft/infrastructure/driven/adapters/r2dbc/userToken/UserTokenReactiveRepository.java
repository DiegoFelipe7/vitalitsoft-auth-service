package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken;

import com.vitalitsoft.domain.shared.enums.TokenType;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserTokenReactiveRepository extends ReactiveCrudRepository<UserToken, UUID>, ReactiveQueryByExampleExecutor<UserToken> {
    Mono<UserToken> findByTokenAndTokenType(String email , TokenType tokenType);
}
