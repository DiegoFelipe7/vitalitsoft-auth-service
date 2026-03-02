package com.vitalitsoft.infrastructure.entry.points.api.auth;

import com.vitalitsoft.application.dto.auth.request.ActivateAccountRequest;
import com.vitalitsoft.application.dto.auth.request.LoginRequest;
import com.vitalitsoft.application.dto.auth.request.RegisterUserRequest;
import com.vitalitsoft.application.dto.auth.response.LoginResponse;
import com.vitalitsoft.application.dto.passwordReset.request.ConfirmPasswordResetRequest;
import com.vitalitsoft.application.dto.passwordReset.request.RequestResetPassword;
import com.vitalitsoft.application.dto.passwordReset.request.ValidateTokenResetRequest;
import com.vitalitsoft.application.usecase.auth.ActivateAccountUseCase;
import com.vitalitsoft.application.usecase.auth.LoginUseCase;
import com.vitalitsoft.application.usecase.auth.RegisterUserUseCase;
import com.vitalitsoft.application.usecase.passwordReset.ConfirmPasswordResetUseCase;
import com.vitalitsoft.application.usecase.passwordReset.RequestResetPasswordUseCase;
import com.vitalitsoft.application.usecase.passwordReset.ValidatePasswordResetTokenUseCase;
import com.vitalitsoft.application.usecase.otp.ResendOtpUseCase;
import com.vitalitsoft.application.usecase.otp.VerifyOtpUseCase;
import com.vitalitsoft.infrastructure.entry.points.api.config.ObjectValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import com.vitalitsoft.application.dto.otp.ResendOtpRequest;
import com.vitalitsoft.application.dto.otp.ValidateOtpRequest;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {
    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final ActivateAccountUseCase activateAccountUseCase;
    private final RequestResetPasswordUseCase requestResetPasswordUseCase;
    private final ValidatePasswordResetTokenUseCase validatePasswordResetTokenUseCase;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    private final ResendOtpUseCase resendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final ObjectValidator objectValidator;


    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(loginRequest -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(loginUseCase.apply(loginRequest.getEmail(), loginRequest.getPassword()), TokenModel.class)
                );
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(RegisterUserRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(registerRequest -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(registerUserUseCase.apply(registerRequest), Void.class)
                );
    }

    public Mono<ServerResponse> activateAccount(ServerRequest request) {
        return request.bodyToMono(ActivateAccountRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(activateAccountRequest -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(activateAccountUseCase.apply(activateAccountRequest), Void.class)
                );
    }

    public Mono<ServerResponse> requestResetPassword(ServerRequest request) {
        return request.bodyToMono(RequestResetPassword.class)
                .flatMap(objectValidator::validate)
                .flatMap(resetRequest -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestResetPasswordUseCase.apply(resetRequest), Void.class)
                );
    }

    public Mono<ServerResponse> validatePasswordResetToken(ServerRequest request) {
        return request.bodyToMono(ValidateTokenResetRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(validateRequest -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(validatePasswordResetTokenUseCase.apply(validateRequest), Boolean.class)
                );
    }

    public Mono<ServerResponse> confirmPasswordReset(ServerRequest request) {
        return request.bodyToMono(ConfirmPasswordResetRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(confirmRequest -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(confirmPasswordResetUseCase.apply(confirmRequest), Void.class)
                );
    }

    public Mono<ServerResponse> resendOtp(ServerRequest request) {
        return request.bodyToMono(ResendOtpRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(req -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(resendOtpUseCase.apply(req), Void.class));
    }

    public Mono<ServerResponse> verifyOtp(ServerRequest request) {
        return request.bodyToMono(ValidateOtpRequest.class)
                .flatMap(objectValidator::validate)
                .flatMap(req -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(verifyOtpUseCase.apply(req), LoginResponse.class));
    }

}
