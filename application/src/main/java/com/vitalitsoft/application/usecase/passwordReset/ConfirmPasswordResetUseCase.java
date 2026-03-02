package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class ConfirmPasswordResetUseCase {
    private final UserTokenRepository userTokenRepository;
    private final AuthRepository authRepository;
    private final HashingRepository hashingRepository;

    public Mono<Void> apply(String token, TokenType tokenType, String newPassword) {
        log.info("Iniciando confirmación de restablecimiento de contraseña para token tipo: {}", tokenType);

        return userTokenRepository.findByTokenAndType(token, tokenType)
                .flatMap(userTokenModel ->
                        updateUserPassword(userTokenModel.getEmail(), newPassword)
                                .then(markTokenAsUsed(userTokenModel))
                                .doOnSuccess(unused -> log.info("Token marcado como usado para: {}", token))
                                .doOnError(error -> log.error("Error al marcar token como usado", error)));

    }


    private Mono<Void> updateUserPassword(String email, String rawPassword) {
        return authRepository.findByEmail(email)
                .flatMap(user -> hashingRepository.hash(rawPassword)
                        .map(hashed -> {
                            user.setPassword(hashed);
                            return user;
                        }))
                .flatMap(authRepository::save)
                .then();
    }

    private Mono<Void> markTokenAsUsed(UserTokenModel token) {
        return userTokenRepository.markAsUsed(token);
    }

}
