package com.vitalitsoft.application.usecase.passwordReset;

import com.vitalitsoft.application.dto.passwordReset.response.ConfirmPasswordResetResponse;
import com.vitalitsoft.application.mapper.passwordReset.PasswordResetResponseMapper;
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

    public Mono<ConfirmPasswordResetResponse> apply(String token, TokenType tokenType, String newPassword) {
        log.info("Iniciando confirmación de restablecimiento de contraseña para token tipo: {}", tokenType);

        return userTokenRepository.findByTokenAndType(token, tokenType)
                .flatMap(userTokenModel ->
                        updateUserPassword(userTokenModel.getEmail(), newPassword)
                                .then(markTokenAsUsed(userTokenModel))
                                .thenReturn(userTokenModel.getEmail())
                                .doOnSuccess(email -> log.info("Token marcado como usado y contraseña actualizada para: {}", email))
                                .doOnError(error -> log.error("Error al confirmar restablecimiento de contraseña", error)))
                .map(PasswordResetResponseMapper::toConfirmPasswordResetResponse)
                .doOnSuccess(response -> log.info("Contraseña restablecida exitosamente para: {}", response.getEmail()));
    }


    private Mono<Void> updateUserPassword(String email, String rawPassword) {
        log.debug("Actualizando contraseña para: {}", email);
        
        return authRepository.findByEmail(email)
                .flatMap(user -> hashingRepository.hash(rawPassword)
                        .map(user::withPassword))
                .flatMap(authRepository::save)
                .then()
                .doOnSuccess(unused -> log.debug("Contraseña actualizada exitosamente para: {}", email));
    }

    private Mono<Void> markTokenAsUsed(UserTokenModel token) {
        log.debug("Marcando token de reset como usado");
        return userTokenRepository.markAsUsed(token);
    }

}
