package com.vitalitsoft.application.usecase.auth;

import com.vitalitsoft.application.dto.auth.request.ActivateAccountRequest;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
public class ActivateAccountUseCase implements Function<ActivateAccountRequest, Mono<Void>> {
    private final AuthRepository authRepository;
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<Void> apply(ActivateAccountRequest request) {
        log.info("Iniciando proceso de activación de cuenta con token tipo: {}", request.getTokenType());
        
        return userTokenRepository.findByTokenAndType(request.getToken(), request.getTokenType())
                .flatMap(userToken ->
                        updateUserStatus(userToken.getEmail())
                                .then(markTokenAsUsed(userToken))
                                .doOnSuccess(email -> log.info("Token marcado como usado para email: {}", email))
                                .doOnError(error -> log.error("Error al marcar token como usado", error)))
                .doOnSuccess(response -> log.info("Cuenta activada exitosamente: {}", request.getToken()));
    }

    private Mono<Void> updateUserStatus(String email) {
        return authRepository.findByEmail(email)
                .flatMap(user -> {
                    user.setStatus(Status.ACTIVE);
                    return authRepository.save(user);
                }).then();
    }

    private Mono<Void> markTokenAsUsed(UserTokenModel token) {
        return userTokenRepository.markAsUsed(token);
    }
}
