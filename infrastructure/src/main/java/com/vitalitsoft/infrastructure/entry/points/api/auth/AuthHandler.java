package com.vitalitsoft.infrastructure.entry.points.api.auth;

import com.vitalitsoft.application.dto.auth.LoginRequest;
import com.vitalitsoft.application.dto.auth.RegisterUserRequest;
import com.vitalitsoft.application.dto.passwordReset.ConfirmPasswordResetRequest;
import com.vitalitsoft.application.dto.passwordReset.RequestResetPassword;
import com.vitalitsoft.application.dto.passwordReset.ValidateTokenResetRequest;
import com.vitalitsoft.application.mapper.auth.AuthMapper;
import com.vitalitsoft.application.mapper.passwordReset.PasswordResetMapper;
import com.vitalitsoft.application.usecase.auth.LoginUseCase;
import com.vitalitsoft.application.usecase.auth.RegisterUserUseCase;
import com.vitalitsoft.application.usecase.passwordReset.ConfirmPasswordResetUseCase;
import com.vitalitsoft.application.usecase.passwordReset.RequestResetPasswordUseCase;
import com.vitalitsoft.application.usecase.passwordReset.ValidatePasswordResetTokenUseCase;
import com.vitalitsoft.infrastructure.entry.points.api.config.ObjectValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {
    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final RequestResetPasswordUseCase requestResetPasswordUseCase;
    private final ValidatePasswordResetTokenUseCase validatePasswordResetTokenUseCase;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    private final ObjectValidator objectValidator;


    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(loginRequest -> loginUseCase.apply(loginRequest.getEmail(), loginRequest.getPassword()))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(RegisterUserRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(registerRequest -> registerUserUseCase.apply(
                        AuthMapper.toAuthModel(registerRequest.getEmail(), registerRequest.getPassword()),
                        AuthMapper.toUserRegisterEventModel(registerRequest)))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> requestResetPassword(ServerRequest request) {
        return request.bodyToMono(RequestResetPassword.class)
                .flatMap(objectValidator::validate)
                .flatMap(resetRequest -> requestResetPasswordUseCase.apply(PasswordResetMapper.toPasswordResetModel(resetRequest.getEmail()))
                )
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> validatePasswordResetToken(ServerRequest request) {
        return request.bodyToMono(ValidateTokenResetRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(validateRequest -> validatePasswordResetTokenUseCase.apply(validateRequest.getToken(), validateRequest.getTokenType()))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> confirmPasswordReset(ServerRequest request) {
        return request.bodyToMono(ConfirmPasswordResetRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(confirmRequest -> confirmPasswordResetUseCase.apply(confirmRequest.getToken(), confirmRequest.getTokenType(), confirmRequest.getPassword()))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

}
