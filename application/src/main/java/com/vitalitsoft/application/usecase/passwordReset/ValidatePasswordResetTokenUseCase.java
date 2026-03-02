package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
public class ValidatePasswordResetTokenUseCase implements BiFunction<String, TokenType, Mono<Boolean>> {
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<Boolean> apply(String token, TokenType tokenType) {
        return this.userTokenRepository.findByTokenAndType(token, tokenType)
                .map(UserTokenModel::isValid)
                .defaultIfEmpty(false)
                .doOnSuccess(isValid -> log.info("Validación de token de tipo: {} resultó en: {}", tokenType, isValid))
                .doOnError(error -> log.error("Error al validar token de tipo: {}", tokenType, error));
    }
}
