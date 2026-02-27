package com.vitalitsoft.domain.auth.gateways;

import com.vitalitsoft.domain.auth.TokenModel;
import reactor.core.publisher.Mono;



public interface JwtRepository {
    Mono<TokenModel> generateToken(String email, String role , Boolean isTwoFactorAuthRequired);
    Mono<Boolean> validateToken(String token);
    Mono<String> getEmailFromToken(String token);

}
