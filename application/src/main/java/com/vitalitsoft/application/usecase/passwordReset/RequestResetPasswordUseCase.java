package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.application.dto.passwordReset.response.RequestResetPasswordResponse;
import com.vitalitsoft.application.mapper.passwordReset.PasswordResetResponseMapper;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.PasswordResetEventModel;
import com.vitalitsoft.domain.shared.enums.UserEventType;
import com.vitalitsoft.domain.shared.events.RabbitEventCatalog;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class RequestResetPasswordUseCase implements Function<UserTokenModel, Mono<RequestResetPasswordResponse>> {

    private final AuthRepository authRepository;
    private final UserTokenRepository userTokenRepository;
    private final EventsRepository<PasswordResetEventModel> eventsRepository;

    @Override
    public Mono<RequestResetPasswordResponse> apply(UserTokenModel userTokenModel) {
        log.info("Iniciando solicitud de restablecimiento de contraseña para: {}", userTokenModel.getEmail());

        return authRepository.findByEmail(userTokenModel.getEmail())
                .map(user -> userTokenModel.withUserId(user.getId()))
                .flatMap(userTokenRepository::save)
                .flatMap(saved -> publishEvent(userTokenModel.getEmail(), userTokenModel.getToken())
                        .thenReturn(userTokenModel.getEmail()))
                .map(PasswordResetResponseMapper::toRequestResetPasswordResponse)
                .doOnSuccess(response -> log.info("Proceso de solicitud de restablecimiento completado para: {}", response.getEmail()))
                .doOnError(error -> log.error("Error al solicitar restablecimiento para {}: {}", userTokenModel.getEmail(), error.getMessage()));
    }

    private Mono<Void> publishEvent(String email, String token) {
        log.debug("Publicando evento de restablecimiento de contraseña para: {}", email);
        var routing = RabbitEventCatalog.resolve(UserEventType.USER_PASSWORD_RESET);
        return eventsRepository.publish(routing.exchange(), routing.routingKey(), PasswordResetEventModel.builder()
                                .email(email)
                                .token(token)
                                .build())
                .doOnSuccess(unused -> log.info("Evento '{}' publicado exitosamente para: {}", routing.exchange(), email))
                .doOnError(error -> log.error("Error al publicar evento '{}' para: {}", routing.exchange(), email, error));

    }
}
