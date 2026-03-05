package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.application.dto.passwordReset.request.RequestResetPassword;
import com.vitalitsoft.application.mapper.userToken.UserTokenMapper;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.PasswordResetEventModel;
import com.vitalitsoft.domain.shared.constants.RabbitEvent;
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
    private final EventsRepository<PasswordResetEventModel> eventPublisher;

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

        PasswordResetEventModel event = PasswordResetEventModel.builder()
                .email(email)
                .token(token)
                .build();

        log.debug("Publicando evento de restablecimiento de contraseña para: {}", email);
        return eventPublisher.publish(RabbitEvent.USER_PASSWORD_RESET, event)
                .doOnSuccess(unused -> log.info("Evento publicado exitosamente para: {}", email))
                .doOnError(error -> log.error("Error al publicar evento para: {}", email, error));

    }
}
