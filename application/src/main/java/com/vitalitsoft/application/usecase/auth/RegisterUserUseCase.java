package com.vitalitsoft.application.usecase.auth;


import com.vitalitsoft.application.dto.auth.request.RegisterUserRequest;
import com.vitalitsoft.application.mapper.auth.AuthMapper;
import com.vitalitsoft.application.mapper.userToken.UserTokenMapper;
import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.UserRegisterEventModel;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.enums.UserEventType;
import com.vitalitsoft.domain.shared.events.RabbitEventCatalog;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.UUID;
import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
public class RegisterUserUseCase implements Function<RegisterUserRequest, Mono<Void>> {
    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;
    private final EventsRepository<UserRegisterEventModel> eventsRepository;
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<Void> apply(RegisterUserRequest request) {

        log.info("Iniciando registro para: {}", request.getEmail());

        return validateEmailNotExists(request.getEmail())
                .then(createAuthModel(request))
                .flatMap(this::saveUserAndToken)
                .flatMap(tuple -> publishEvent(tuple.getT1().getId(), tuple.getT2().getToken(), request))
                .doOnSuccess(response -> log.info("Registro completado exitosamente para: {}", request.getEmail()))
                .doOnError(error -> log.error("Error en registro de usuario {}: {}", request.getEmail(), error.getMessage()));

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

    private Mono<AuthModel> createAuthModel(RegisterUserRequest request) {
        AuthModel authModel = AuthMapper.toAuthModel(request.getEmail(), request.getPassword());
        return hashPassword(authModel);
    }


    private Mono<AuthModel> hashPassword(AuthModel authModel) {
        return hashingRepository.hash(authModel.getPassword())
                .map(hashedPassword -> {
                    authModel.setPassword(hashedPassword);
                    return authModel;
                });
    }

    private Mono<Tuple2<AuthModel, UserTokenModel>> saveUserAndToken(AuthModel authModel) {
        log.debug("Guardando usuario y generando token de activación para: {}", authModel.getEmail());

        UserTokenModel tokenModel = UserTokenMapper.toActivateModel(authModel.getId(), authModel.getEmail());

        return Mono.zip(authRepository.save(authModel), userTokenRepository.save(tokenModel))
                .doOnSuccess(result -> log.debug("token generado para: {}", authModel.getEmail()))
                .doOnError(error -> log.error("Error al guardar usuario {}: {}", authModel.getEmail(), error.getMessage()));
    }


    private Mono<Void> publishEvent(UUID userId, String token, RegisterUserRequest request) {
        UserRegisterEventModel eventModel = UserRegisterEventModel.builder()
                .userId(userId)
                .tokenActiveAccount(token)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();

        var routing = RabbitEventCatalog.resolve(UserEventType.USER_CREATED);

        return eventsRepository.publish(routing.exchange(), routing.routingKey(), eventModel)
                .doOnSuccess(unused -> log.info("Evento USER_CREATED publicado para {}", eventModel.getEmail()))
                .doOnError(error -> log.error("Error al publicar evento USER_CREATED para {}: {}", eventModel.getEmail(), error.getMessage()));
    }
}