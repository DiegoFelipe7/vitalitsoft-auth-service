package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.application.command.passwordReset.ValidateTokenResetCommand;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ValidatePasswordResetTokenUseCase implements Function<ValidateTokenResetCommand, Mono<Boolean>> {
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<Boolean> apply(ValidateTokenResetCommand command) {
        log.info("Validando token de tipo: {}", command.getTokenType());

        return this.userTokenRepository.findByTokenAndType(command.getToken(), command.getTokenType())
                .doOnNext(UserTokenModel::verifyValidity)
                .map(ele -> !ele.getUsed())
                .doOnSuccess(isValid -> log.info("Validación de token de tipo: {} resultó en: {}", command.getTokenType(), isValid))
                .doOnError(error -> log.error("Error al validar token de tipo: {}", command.getTokenType(), error));
    }
}
