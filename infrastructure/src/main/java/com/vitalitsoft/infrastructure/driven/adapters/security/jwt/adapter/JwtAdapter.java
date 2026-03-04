package com.vitalitsoft.infrastructure.driven.adapters.security.jwt.adapter;

import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.infrastructure.driven.adapters.security.jwt.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAdapter implements JwtRepository {

    private final JwtProvider jwtProvider;

    @Override
    public Mono<TokenModel> generateToken(String email,
                                          String role,
                                          Boolean isTwoFactorAuthRequired) {

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        UserDetails userDetails = new User(email, "", authorities);

        String accessToken = jwtProvider.generateAccessToken(userDetails, email);
        String rawRefreshToken = jwtProvider.generateRefreshToken(userDetails, email);

        return Mono.just(TokenModel.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .isTwoFactorAuthRequired(isTwoFactorAuthRequired)
                .build());
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> jwtProvider.validate(token));
    }

    @Override
    public Mono<String> getSubject(String token) {
        return Mono.fromCallable(() -> jwtProvider.getSubject(token));
    }


    @Override
    public Mono<String> getEmailFromToken(String token) {
        return Mono.fromCallable(() -> jwtProvider.getClaims(token).get("email", String.class));
    }
}
