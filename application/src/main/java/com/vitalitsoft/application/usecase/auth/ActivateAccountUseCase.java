package com.vitalitsoft.application.usecase.auth;

import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;


@Slf4j
@RequiredArgsConstructor
public class ActivateAccountUseCase implements BiFunction<String, TokenType, Mono<Void>> {
    private final AuthRepository authRepository;
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<Void> apply(String token, TokenType tokenType) {
        log.info("Iniciando proceso de activación de cuenta con token tipo: {}", tokenType);
        
        return userTokenRepository.findByTokenAndType(token, tokenType)
                .flatMap(userToken ->
                        updateUserStatus(userToken.getEmail())
                                .then(markTokenAsUsed(userToken))
                                .doOnSuccess(unused -> log.info("Token marcado como usado para: {}", token))
                                .doOnError(error -> log.error("Error al marcar token como usado", error)));

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
