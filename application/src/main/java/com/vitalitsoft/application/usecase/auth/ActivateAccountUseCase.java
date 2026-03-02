package com.vitalitsoft.application.usecase.auth;

import com.vitalitsoft.application.dto.auth.response.ActivateAccountResponse;
import com.vitalitsoft.application.mapper.auth.AuthResponseMapper;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;


@Slf4j
@RequiredArgsConstructor
public class ActivateAccountUseCase implements BiFunction<String, TokenType, Mono<ActivateAccountResponse>> {
    private final AuthRepository authRepository;
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<ActivateAccountResponse> apply(String token, TokenType tokenType) {
        log.info("Iniciando proceso de activación de cuenta con token tipo: {}", tokenType);
        
        return userTokenRepository.findByTokenAndType(token, tokenType)
                .flatMap(userToken ->
                        updateUserStatus(userToken.getEmail())
                                .then(markTokenAsUsed(userToken))
                                .thenReturn(userToken.getEmail())
                                .doOnSuccess(email -> log.info("Token marcado como usado para email: {}", email))
                                .doOnError(error -> log.error("Error al marcar token como usado", error)))
                .map(AuthResponseMapper::toActivateAccountResponse)
                .doOnSuccess(response -> log.info("Cuenta activada exitosamente: {}", response.getEmail()));
    }

    private Mono<Void> updateUserStatus(String email) {
        log.debug("Actualizando estado del usuario: {}", email);
        return authRepository.findByEmail(email)
                .map(user -> user.withStatus(Status.ACTIVE))
                .flatMap(authRepository::save)
                .then()
                .doOnSuccess(unused -> log.debug("Estado actualizado para: {}", email));
    }

    private Mono<Void> markTokenAsUsed(UserTokenModel token) {
        log.debug("Marcando token como usado");
        return userTokenRepository.markAsUsed(token);
    }
}
