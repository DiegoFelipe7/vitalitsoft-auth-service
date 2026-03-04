package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.application.dto.passwordReset.request.RequestResetPassword;
import com.vitalitsoft.application.mapper.userToken.UserTokenMapper;
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
public class RequestResetPasswordUseCase implements Function<RequestResetPassword, Mono<Void>> {

    private final AuthRepository authRepository;
    private final UserTokenRepository userTokenRepository;
    private final EventsRepository<PasswordResetEventModel> eventsRepository;

    @Override
    public Mono<Void> apply(RequestResetPassword request) {
        log.info("Iniciando solicitud de restablecimiento de contraseña para: {}", request.getEmail());

        return authRepository.findByEmail(request.getEmail())
                .map(user -> {
                    UserTokenModel userTokenModel = UserTokenMapper.toPasswordModel(request.getEmail());
                    return userTokenModel.withUserId(user.getId());
                })
                .flatMap(userTokenRepository::save)
                .flatMap(saved -> publishEvent(request.getEmail(), saved.getToken()))
                .doOnSuccess(response -> log.info("Proceso de solicitud de restablecimiento completado para: {}", request.getEmail()))
                .doOnError(error -> log.error("Error al solicitar restablecimiento para {}: {}", request.getEmail(), error.getMessage()));
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
