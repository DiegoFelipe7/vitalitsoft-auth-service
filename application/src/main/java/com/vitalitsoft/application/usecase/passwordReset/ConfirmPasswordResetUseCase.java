package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class ConfirmPasswordResetUseCase {
    private final UserTokenRepository userTokenRepository;
    private final AuthRepository authRepository;

    public Mono<Void> apply(String token, TokenType tokenType, String newPassword) {
        log.info("Iniciando confirmación de restablecimiento de contraseña para token tipo: {}", tokenType);
        
        return userTokenRepository.findByTokenAndType(token, tokenType)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Token inválido o expirado para tipo: {}", tokenType);
                    return Mono.error(new NexusException(
                            "TOKEN INVÁLIDO O EXPIRADO",
                            HttpStatus.UNAUTHORIZED
                    ));
                }))
                .flatMap(userTokenModel -> {
                    log.debug("Token encontrado para email: {}", userTokenModel.getEmail());
                    return authRepository.findByEmail(userTokenModel.getEmail())
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("Usuario no encontrado para email asociado al token: {}", userTokenModel.getEmail());
                                return Mono.error(new NexusException(
                                        "USUARIO NO ENCONTRADO",
                                        HttpStatus.NOT_FOUND
                                ));
                            }))
                            .flatMap(user -> {
                                log.info("Actualizando contraseña para usuario: {}", user.getEmail());
                                user.setPassword(newPassword);
                                return authRepository.saveUser(user)
                                        .doOnSuccess(userId -> log.info("Contraseña actualizada exitosamente para: {}", user.getEmail()))
                                        .doOnError(error -> log.error("Error al actualizar contraseña para: {}", user.getEmail(), error));
                            })
                            .then(userTokenRepository.markAsUsed(userTokenModel))
                            .doOnSuccess(unused -> log.info("Token marcado como usado para: {}", userTokenModel.getEmail()))
                            .doOnError(error -> log.error("Error al marcar token como usado", error));
                })
                .doOnSuccess(unused -> log.info("Proceso de restablecimiento de contraseña completado exitosamente"));
    }
}
