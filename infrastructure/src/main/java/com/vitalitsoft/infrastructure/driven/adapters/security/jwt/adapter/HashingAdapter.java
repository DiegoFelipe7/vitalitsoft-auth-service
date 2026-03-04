package com.vitalitsoft.infrastructure.driven.adapters.security.jwt.adapter;

import com.vitalitsoft.domain.hashing.HashingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class HashingAdapter implements HashingRepository {
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<String> hash(String rawValue) {
        return Mono.fromCallable(() -> passwordEncoder.encode(rawValue))
                .subscribeOn(Schedulers.boundedElastic());

    }

    @Override
    public Mono<Boolean> matches(String rawValue, String hashedValue) {
        return Mono.fromCallable(() -> passwordEncoder.matches(rawValue, hashedValue))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
