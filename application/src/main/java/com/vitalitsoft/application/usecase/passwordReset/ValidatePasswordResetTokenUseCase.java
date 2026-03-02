package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.application.dto.passwordReset.response.ValidatePasswordResetTokenResponse;
import com.vitalitsoft.application.mapper.passwordReset.PasswordResetResponseMapper;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
public class ValidatePasswordResetTokenUseCase implements BiFunction<String, TokenType, Mono<ValidatePasswordResetTokenResponse>> {
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<ValidatePasswordResetTokenResponse> apply(String token, TokenType tokenType) {
        log.info("Validando token de tipo: {}", tokenType);
        
        return this.userTokenRepository.findByTokenAndType(token, tokenType)
                .map(UserTokenModel::isValid)
                .defaultIfEmpty(false)
                .map(PasswordResetResponseMapper::toValidatePasswordResetTokenResponse)
                .doOnSuccess(response -> log.info("Validación de token de tipo: {} resultó en: {}", tokenType, response.getIsValid()))
                .doOnError(error -> log.error("Error al validar token de tipo: {}", tokenType, error));
    }
}
