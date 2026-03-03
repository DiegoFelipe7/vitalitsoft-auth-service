package com.vitalitsoft.application.usecase.auth;

import com.vitalitsoft.application.dto.auth.request.ActivateAccountRequest;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
public class ActivateAccountUseCase implements Function<ActivateAccountRequest, Mono<Void>> {
    private final AuthRepository authRepository;
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<Void> apply(ActivateAccountRequest request) {
        log.info("Iniciando proceso de activación de cuenta con token tipo: {}", TokenType.ACTIVATE_ACCOUNT);

        return userTokenRepository.findByTokenAndType(request.getToken(), TokenType.ACTIVATE_ACCOUNT)
                .flatMap(userToken ->
                        updateUserStatus(userToken.getEmail())
                                .then(markTokenAsUsed(userToken))
                                .doOnSuccess(email -> log.info("Token marcado como usado para email: {}", email))
                                .doOnError(error -> log.error("Error al marcar token como usado", error)))
                .doOnSuccess(response -> log.info("Cuenta activada exitosamente: {}", request.getToken()));
    }

    private Mono<Void> updateUserStatus(String email) {
        log.debug("Actualizando estado del usuario: {}", email);

        return authRepository.findByEmail(email)
                .map(user -> user
                        .withId(user.getId())
                        .withStatus(Status.ACTIVE)
                        .withUpdatedAt(LocalDateTime.now())
                )
                .flatMap(authRepository::save)
                .then()
                .doOnSuccess(unused -> log.debug("Estado actualizado para: {}", email))
                .doOnError(error -> log.error("Error al actualizar estado del usuario: {}", email, error));
    }

    private Mono<Void> markTokenAsUsed(UserTokenModel token) {
        log.debug("Marcando token como usado");
        return userTokenRepository.markAsUsed(token);
    }
}
