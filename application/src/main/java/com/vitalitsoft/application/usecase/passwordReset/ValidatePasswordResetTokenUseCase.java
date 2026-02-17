package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
public class ValidatePasswordResetTokenUseCase implements BiFunction<String, TokenType, Mono<Boolean>> {
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<Boolean> apply(String token, TokenType tokenType) {
        log.info("Validando token de tipo: {}", tokenType);
        return this.userTokenRepository.findByTokenAndType(token, tokenType)
                .map(ele -> ele.getUsed() == Boolean.FALSE && ele.getExpirationTime().isAfter(LocalDateTime.now()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Token no encontrado para tipo: {}", tokenType);
                    return Mono.just(false);
                }))
                .doOnError(error -> log.error("Error al validar token de tipo: {}", tokenType, error));
    }
}
