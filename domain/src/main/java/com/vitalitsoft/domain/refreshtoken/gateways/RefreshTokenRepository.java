package com.vitalitsoft.domain.refreshtoken.gateways;

import com.vitalitsoft.domain.refreshtoken.RefreshTokenModel;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefreshTokenRepository {
    Mono<Void> save(String email, UUID userId , String token);
    Mono<RefreshTokenModel> findByToken(String token);
    Mono<Void> revokeByEmail(String email);
}
