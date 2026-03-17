package com.vitalitsoft.infrastructure.driven.adapters.security.jwt.manager;


import com.vitalitsoft.domain.shared.exception.VitalitSoftException;
import com.vitalitsoft.infrastructure.driven.adapters.security.jwt.provider.JwtProvider;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtProvider jwtProvider;

    @Override
    @Nonnull
    public Mono<Authentication> authenticate(@Nonnull Authentication authentication) {
        return Mono.just(authentication)
                .map(auth -> jwtProvider.getClaims(Objects.requireNonNull(auth.getCredentials()).toString()))
                .onErrorResume(e -> Mono.error(new VitalitSoftException("BAD TOKEN")))
                .map(claims -> {
                    Object rolesObj = claims.get("roles");
                    List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                    if (rolesObj instanceof List<?> rawList) {
                        authorities = rawList.stream()
                                .filter(Map.class::isInstance)
                                .map(m -> (Map<?, ?>) m)
                                .map(m -> m.get("authority"))
                                .filter(String.class::isInstance)
                                .map(auth -> new SimpleGrantedAuthority((String) auth))
                                .toList();
                    }

                    return new UsernamePasswordAuthenticationToken(
                            claims.getSubject(),
                            null,
                            authorities
                    );
                });
    }
}