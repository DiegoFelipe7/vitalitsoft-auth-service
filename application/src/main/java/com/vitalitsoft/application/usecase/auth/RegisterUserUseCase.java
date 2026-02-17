package com.vitalitsoft.application.usecase.auth;

import co.com.bancolombia.model.auth.AuthModel;
import co.com.bancolombia.model.auth.gateways.AuthRepository;
import co.com.bancolombia.model.auth.gateways.PasswordRepository;
import co.com.bancolombia.model.events.gateways.EventsRepository;
import co.com.bancolombia.model.events.model.UserRegisterEventModel;
import co.com.bancolombia.model.shared.constants.HttpStatus;
import co.com.bancolombia.model.shared.enums.UserEventType;
import co.com.bancolombia.model.shared.events.RabbitEventCatalog;
import co.com.bancolombia.model.shared.exception.NexusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
public class RegisterUserUseCase implements BiFunction<AuthModel, UserRegisterEventModel, Mono<Void>> {
    private final AuthRepository authRepository;
    private final PasswordRepository passwordRepository;
    private final EventsRepository<UserRegisterEventModel> eventsRepository;

    @Override
    public Mono<Void> apply(AuthModel authModel, UserRegisterEventModel userRegisterEventModel) {
        log.info("Iniciando proceso de registro para el usuario: {}", authModel.getEmail());
        return authRepository.findByEmail(authModel.getEmail())
                .flatMap(existing -> {
                    log.warn("Intento de registro con email ya existente: {}", authModel.getEmail());
                    return Mono.error(new NexusException(
                            "EL EMAIL YA SE ENCUENTRA REGISTRADO",
                            HttpStatus.CONFLICT
                    ));
                })
                .switchIfEmpty(
                        Mono.defer(() -> {
                            log.debug("Email disponible, procediendo con encriptación de contraseña para: {}", authModel.getEmail());
                            String hashed = passwordRepository.encryptPassword(authModel.getPassword());
                            authModel.setPassword(hashed);
                            return authRepository.saveUser(authModel)
                                    .doOnSuccess(userId -> log.info("Usuario guardado exitosamente con ID: {} para email: {}", userId, authModel.getEmail()))
                                    .flatMap(userId -> {
                                        userRegisterEventModel.setUserId(userId);
                                        return publishUserRegisteredEvent(userRegisterEventModel);
                                    })
                                    .doOnError(error -> log.error("Error al guardar usuario: {}", authModel.getEmail(), error));
                        })
                )
                .then()
                .doOnSuccess(unused -> log.info("Registro completado exitosamente para: {}", authModel.getEmail()));
    }

    private Mono<Void> publishUserRegisteredEvent(UserRegisterEventModel event) {
        log.debug("Publicando evento de registro para: {}", event.getEmail());
        var routing = RabbitEventCatalog.resolve(UserEventType.USER_CREATED);
        return eventsRepository.publish(routing.exchange(), routing.routingKey(), event)
                .doOnSuccess(unused -> log.info("Evento '{}' publicado exitosamente para: {}", routing.exchange(), event.getEmail()))
                .doOnError(error -> log.error("Error al publicar evento '{}' para: {}", routing.exchange(), event.getEmail(), error))
                .onErrorMap(error -> new NexusException(
                        "Error al publicar evento de registro: " + error.getMessage(),
                        HttpStatus.BAD_REQUEST
                ));
    }
}
