package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.enums.UserEventType;
import com.vitalitsoft.domain.shared.events.RabbitEventCatalog;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class RequestResetPasswordUseCase implements Function<UserTokenModel, Mono<Void>> {

    private final AuthRepository authRepository;
    private final UserTokenRepository userTokenRepository;
    private final EventsRepository<String> eventsRepository;

    @Override
    public Mono<Void> apply(UserTokenModel request) {
        log.info("Iniciando solicitud de restablecimiento de contraseña para: {}", request.getEmail());
        
        return authRepository.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Solicitud de reset de contraseña para usuario inexistente: {}", request.getEmail());
                    return Mono.error(new NexusException(
                            NexusException.Type.USER_NOT_FOUND,
                            HttpStatus.NOT_FOUND
                    ));
                }))
                .flatMap(user -> {
                    log.debug("Usuario encontrado: {}, guardando solicitud de token", user.getEmail());
                    request.setUserId(user.getId());
                    return userTokenRepository.saveRequest(request)
                            .doOnSuccess(saved -> log.info("Token de restablecimiento guardado para: {}", user.getEmail()))
                            .doOnError(error -> log.error("Error al guardar token de restablecimiento para: {}", user.getEmail(), error));
                })
                .flatMap(saved -> publishPasswordResetEvent(request.getEmail()))
                .doOnSuccess(unused -> log.info("Proceso de solicitud de restablecimiento completado para: {}", request.getEmail()));
    }

    private Mono<Void> publishPasswordResetEvent(String email) {
        log.debug("Publicando evento de restablecimiento de contraseña para: {}", email);
        var routing = RabbitEventCatalog.resolve(UserEventType.USER_PASSWORD_RESET);
        return eventsRepository.publish(routing.exchange(), routing.routingKey(), email)
                .doOnSuccess(unused -> log.info("Evento '{}' publicado exitosamente para: {}", routing.exchange(), email))
                .doOnError(error -> log.error("Error al publicar evento '{}' para: {}", routing.exchange(), email, error))
                .onErrorMap(error -> new NexusException(
                        NexusException.Type.INTERNAL_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }
}
