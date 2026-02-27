package com.vitalitsoft.application.usecase.auth;


import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.UserRegisterEventModel;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.enums.UserEventType;
import com.vitalitsoft.domain.shared.events.RabbitEventCatalog;
import com.vitalitsoft.domain.shared.exception.NexusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;


@Slf4j
@RequiredArgsConstructor
public class RegisterUserUseCase implements BiFunction<AuthModel, UserRegisterEventModel, Mono<Void>> {
    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;
    private final EventsRepository<UserRegisterEventModel> eventsRepository;

    @Override
    public Mono<Void> apply(AuthModel authModel, UserRegisterEventModel eventModel) {

        log.info("Iniciando registro para: {}", authModel.getEmail());

        return validateEmailNotExists(authModel.getEmail())
                .then(createUser(authModel))
                .flatMap(savedUser -> publishEvent(savedUser, eventModel))
                .doOnSuccess(unused ->
                        log.info("Registro completado exitosamente para: {}", authModel.getEmail())
                );
    }

    private Mono<Void> validateEmailNotExists(String email) {
        return authRepository.findByEmail(email)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Email ya registrado: {}", email);
                        return Mono.error(new NexusException(
                                NexusException.Type.EMAIL_ALREADY_REGISTERED,
                                HttpStatus.CONFLICT
                        ));
                    }
                    return Mono.empty();
                });
    }

    private Mono<AuthModel> createUser(AuthModel authModel) {
        return hashingRepository.hash(authModel.getPassword())
                .map(hashedPassword -> {
                    authModel.setPassword(hashedPassword);
                    return authModel;
                })
                .flatMap(authRepository::save)
                .doOnSuccess(user -> log.info("Usuario creado  para email: {}", authModel.getEmail()))
                .onErrorMap(error ->
                        new NexusException(
                                NexusException.Type.INTERNAL_ERROR,
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                );
    }


    private Mono<Void> publishEvent(AuthModel savedUser, UserRegisterEventModel eventModel) {

        var routing = RabbitEventCatalog.resolve(UserEventType.USER_CREATED);
        eventModel.setUserId(savedUser.getId());

        log.debug("Publicando evento USER_CREATED para: {}", eventModel.getEmail());

        return eventsRepository
                .publish(routing.exchange(), routing.routingKey(), eventModel)
                .doOnSuccess(unused -> log.info("Evento publicado correctamente para: {}", eventModel.getEmail()))
                .onErrorMap(error ->
                        new NexusException(
                                NexusException.Type.INTERNAL_ERROR,
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                );
    }
}