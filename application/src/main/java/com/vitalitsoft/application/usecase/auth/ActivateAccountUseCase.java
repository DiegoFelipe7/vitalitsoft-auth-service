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
import java.util.UUID;
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
                .doOnNext(UserTokenModel::verifyValidity)
                .flatMap(this::activateUserAccount)
                .flatMap(ele -> markTokenAsUsed(ele.getId()))
                .doOnSuccess(unused -> log.info("Cuenta activada exitosamente - Token: {}", request.getToken()))
                .doOnError(error -> log.error("Error en activación de cuenta - Token: {}, Error: {}", request.getToken(), error.getMessage()));

    }

    private Mono<UserTokenModel> activateUserAccount(UserTokenModel userTokenModel) {

        return authRepository.findByEmail(userTokenModel.getEmail())
                .map(user -> user
                        .withId(user.getId())
                        .withStatus(Status.ACTIVE)
                        .withUpdatedAt(LocalDateTime.now())
                )
                .flatMap(res -> authRepository.updateStatus(res.getId(), res.getStatus()))
                .doOnSuccess(unused -> log.debug("Estado actualizado para: {}", userTokenModel.getEmail()))
                .doOnError(error -> log.error("Error al actualizar estado del usuario: {}", userTokenModel.getEmail(), error))
                .thenReturn(userTokenModel);
    }

    private Mono<Void> markTokenAsUsed(UUID uuid) {
        log.debug("Marcando token como usado");
        return userTokenRepository.markAsUsed(uuid);
    }
}
