package com.vitalitsoft.domain.auth.gateways;

import reactor.core.publisher.Mono;

public interface PasswordRepository {
    String encryptPassword(String password);
    Mono<Boolean> matchPassword(String rawPassword, String encodedPassword);
}
