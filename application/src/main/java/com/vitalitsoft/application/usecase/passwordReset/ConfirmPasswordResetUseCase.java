package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.application.dto.passwordReset.request.ConfirmPasswordResetRequest;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;
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
                .flatMap(userTokenModel -> updateUserPassword(userTokenModel, request.getPassword()))
                .flatMap(this::markTokenAsUsed)
                .doOnSuccess(email -> log.info("Token marcado como usado y contraseña actualizada para: {}", email))
                .doOnError(error -> log.error("Error al confirmar restablecimiento de contraseña", error));

    }


    private Mono<UUID> updateUserPassword(UserTokenModel userTokenModel, String rawPassword) {
        log.debug("Actualizando contraseña para: {}", userTokenModel.getEmail());
        return authRepository.findByEmail(userTokenModel.getEmail())
                .flatMap(user -> hashingRepository.hash(rawPassword).map(user::withPassword))
                .flatMap(res->authRepository.updatePassword(userTokenModel.getUserId(), res.getPassword()))
                .thenReturn(userTokenModel.getUserId())
                .doOnSuccess(unused -> log.debug("Contraseña actualizada exitosamente para: {}", userTokenModel.getEmail()));
    }

    private Mono<Void> markTokenAsUsed(UUID uuid) {
        log.debug("Marcando token de reset como usado");
        return userTokenRepository.markAsUsed(uuid);
    }

}
