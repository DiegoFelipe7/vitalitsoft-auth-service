package com.vitalitsoft.application.usecase.auth;

import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;


@Slf4j
@RequiredArgsConstructor
public class ActivateAccountUseCase implements BiFunction<String, TokenType, Mono<Void>> {
    private final AuthRepository authRepository;
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<Void> apply(String token, TokenType tokenType) {
        log.info("Iniciando proceso de activación de cuenta con token tipo: {}", tokenType);
        
        return userTokenRepository.findByTokenAndType(token, tokenType)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Token no encontrado o inválido para tipo: {}", tokenType);
                    return Mono.error(new NexusException(
                            NexusException.Type.INVALID_TOKEN,
                            HttpStatus.UNAUTHORIZED
                    ));
                }))
                .flatMap(userToken -> {
                    log.debug("Token válido encontrado para email: {}", userToken.getEmail());
                    return authRepository.findByEmail(userToken.getEmail())
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("Usuario no encontrado para email asociado al token: {}", userToken.getEmail());
                                return Mono.error(new NexusException(
                                        NexusException.Type.USER_NOT_FOUND,
                                        HttpStatus.NOT_FOUND
                                ));
                            }))
                            .flatMap(user -> {
                                log.info("Activando cuenta para usuario: {}", user.getEmail());
                                user.setStatus(Status.ACTIVE);
                                return authRepository.save(user)
                                        .doOnSuccess(userId -> log.info("Usuario activado exitosamente: {}", user.getEmail()))
                                        .doOnError(error -> log.error("Error al activar usuario: {}", user.getEmail(), error));
                            })
                            .then(userTokenRepository.markAsUsed(userToken))
                            .doOnSuccess(unused -> log.info("Token marcado como usado para: {}", userToken.getEmail()))
                            .doOnError(error -> log.error("Error al marcar token como usado", error));
                })
                .doOnSuccess(unused -> log.info("Proceso de activación completado exitosamente"));
    }
}
