package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.application.dto.passwordReset.request.ValidateTokenResetRequest;
import com.vitalitsoft.application.dto.passwordReset.response.ValidatePasswordResetTokenResponse;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ValidatePasswordResetTokenUseCase implements Function<ValidateTokenResetRequest, Mono<ValidatePasswordResetTokenResponse>> {
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<ValidatePasswordResetTokenResponse> apply(ValidateTokenResetRequest request) {
        log.info("Validando token de tipo: {}", request.getTokenType());

        return this.userTokenRepository.findByTokenAndType(request.getToken(), request.getTokenType())
                .map(UserTokenModel::isValid)
                .defaultIfEmpty(false)
                .map(ele -> ValidatePasswordResetTokenResponse.builder().isValid(ele).build())
                .doOnSuccess(response -> log.info("Validación de token de tipo: {} resultó en: {}", request.getTokenType(), response.getIsValid()))
                .doOnError(error -> log.error("Error al validar token de tipo: {}", request.getTokenType(), error));
    }
}
