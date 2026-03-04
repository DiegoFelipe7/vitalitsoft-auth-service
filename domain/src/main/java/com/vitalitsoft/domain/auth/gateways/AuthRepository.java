package com.vitalitsoft.domain.auth.gateways;

import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.shared.enums.Status;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface AuthRepository {

    Mono<AuthModel> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    Mono<AuthModel> save(AuthModel authModel);

    Mono<Void> updatePassword(UUID uuid, String newPassword);

    Mono<AuthModel> updateStatus(UUID id, Status status);

    Mono<AuthModel> findById(UUID id);
}
