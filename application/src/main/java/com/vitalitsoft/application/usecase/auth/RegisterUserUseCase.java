package com.vitalitsoft.application.usecase.auth;


import com.vitalitsoft.application.command.auth.RegisterUserCommand;
import com.vitalitsoft.application.mapper.auth.AuthMapper;
import com.vitalitsoft.application.mapper.userToken.UserTokenMapper;
import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.UserRegisterEventModel;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.constants.RabbitEvent;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
public class RegisterUserUseCase implements Function<RegisterUserCommand, Mono<Void>> {
    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;
    private final EventsRepository<UserRegisterEventModel> eventPublisher;
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<Void> apply(RegisterUserCommand command) {

        log.info("Iniciando registro para: {}", command.getEmail());

        return validateEmailNotExists(command.getEmail())
                .then(createAuthModel(command))
                .flatMap(this::saveUserAndToken)
                .flatMap(user -> publishEvent(user.getUserId(), user.getEmail(), command))
                .doOnSuccess(response -> log.info("Registro completado exitosamente para: {}", command.getEmail()))
                .doOnError(error -> log.error("Error en registro de usuario {}: {}", command.getEmail(), error.getMessage()));

    }

    private Mono<Void> validateEmailNotExists(String email) {
        return authRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Intento de registro con email ya existente: {}", email);
                        return Mono.error(new NexusException(
                                NexusException.Type.EMAIL_ALREADY_EXISTS,
                                HttpStatus.CONFLICT
                        ));
                    }
                    return Mono.empty();
                });
    }

    private Mono<AuthModel> createAuthModel(RegisterUserCommand command) {
        AuthModel authModel = AuthMapper.toAuthModel(command.getEmail(), command.getPassword());
        return hashPassword(authModel);
    }


    private Mono<AuthModel> hashPassword(AuthModel authModel) {
        return hashingRepository.hash(authModel.getPassword())
                .map(authModel::withPassword);
    }

    private Mono<UserTokenModel> saveUserAndToken(AuthModel authModel) {
        log.debug("Guardando usuario y generando token de activación para: {}", authModel.getEmail());


        return authRepository.save(authModel)
                .flatMap(auth -> {
                            UserTokenModel tokenModel = UserTokenMapper.toActivateModel(auth.getId(), auth.getEmail());
                            return userTokenRepository.save(tokenModel)
                                    .doOnSuccess(result -> log.debug("token generado para: {}", authModel.getEmail()))
                                    .doOnError(error -> log.error("Error al guardar usuario {}: {}", authModel.getEmail(), error.getMessage()));
                        }
                );

    }


    private Mono<Void> publishEvent(UUID userId, String token, RegisterUserCommand command) {
        UserRegisterEventModel eventModel = UserRegisterEventModel.builder()
                .userId(userId)
                .tokenActiveAccount(token)
                .firstName(command.getFirstName())
                .lastName(command.getLastName())
                .email(command.getEmail())
                .phoneNumber(command.getPhoneNumber())
                .build();

        return eventPublisher.publish(RabbitEvent.USER_CREATED, eventModel)
                .doOnSuccess(unused -> log.info("Evento USER_CREATED publicado para {}", eventModel.getEmail()))
                .doOnError(error -> log.error("Error al publicar evento USER_CREATED para {}: {}", eventModel.getEmail(), error.getMessage()));
    }
}