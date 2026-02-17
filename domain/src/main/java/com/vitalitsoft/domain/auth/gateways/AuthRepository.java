package com.vitalitsoft.domain.auth.gateways;

import com.vitalitsoft.domain.auth.AuthModel;
import reactor.core.publisher.Mono;


public interface AuthRepository {

    Mono<AuthModel> findByEmail(String email);
    Mono<AuthModel> save(AuthModel authModel);
}
