package com.vitalitsoft.domain.hashing;

import reactor.core.publisher.Mono;

public interface HashingRepository {
    Mono<String> hash(String rawValue);
    Mono<Boolean> matches(String rawValue, String hashedValue);
}
