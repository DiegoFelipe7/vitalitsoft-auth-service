package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.application.dto.passwordReset.request.ConfirmPasswordResetRequest;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ConfirmPasswordResetUseCase implements Function<ConfirmPasswordResetRequest, Mono<Void>> {
    private final UserTokenRepository userTokenRepository;
    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;

    @Override
    public Mono<Void> apply(ConfirmPasswordResetRequest request) {
        log.info("Iniciando confirmación de restablecimiento de contraseña para token tipo: {}", request.getTokenType());

        return userTokenRepository.findByTokenAndType(request.getToken(), request.getTokenType())
                .flatMap(userTokenModel ->
                        updateUserPassword(userTokenModel.getEmail(), request.getPassword())
                                .then(markTokenAsUsed(userTokenModel))
                                .doOnSuccess(email -> log.info("Token marcado como usado y contraseña actualizada para: {}", email))
                                .doOnError(error -> log.error("Error al confirmar restablecimiento de contraseña", error)))
                .doOnSuccess(response -> log.info("Contraseña restablecida exitosamente para: {}", request.getToken()));
    }


    private Mono<Void> updateUserPassword(String email, String rawPassword) {
        log.debug("Actualizando contraseña para: {}", email);
        return authRepository.findByEmail(email)
                .flatMap(user -> hashingRepository.hash(rawPassword)
                        .map(user::withPassword))
                .flatMap(authRepository::save)
                .then()
                .doOnSuccess(unused -> log.debug("Contraseña actualizada exitosamente para: {}", email));
    }

    private Mono<Void> markTokenAsUsed(UserTokenModel token) {
        log.debug("Marcando token de reset como usado");
        return userTokenRepository.markAsUsed(token);
    }

}
