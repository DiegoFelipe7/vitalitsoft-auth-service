package com.vitalitsoft.infrastructure.entry.points.api.auth;

import com.vitalitsoft.application.usecase.auth.*;
import com.vitalitsoft.application.usecase.otp.ResendOtpUseCase;
import com.vitalitsoft.application.usecase.otp.VerifyOtpUseCase;
import com.vitalitsoft.application.usecase.passwordReset.ConfirmPasswordResetUseCase;
import com.vitalitsoft.application.usecase.passwordReset.RequestResetPasswordUseCase;
import com.vitalitsoft.application.usecase.passwordReset.ValidatePasswordResetTokenUseCase;
import com.vitalitsoft.domain.shared.constants.Constants;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.auth.request.ActivateAccountRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.auth.request.LoginRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.auth.request.RegisterUserRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.otp.request.ResendOtpRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.otp.request.ValidateOtpRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.passwordReset.request.ConfirmPasswordResetRequest;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.passwordReset.request.RequestResetPassword;
import com.vitalitsoft.infrastructure.entry.points.api.auth.dto.passwordReset.request.ValidateTokenResetRequest;
import com.vitalitsoft.infrastructure.entry.points.api.config.ObjectValidator;
import com.vitalitsoft.infrastructure.entry.points.api.manager.CookieManager;
import com.vitalitsoft.infrastructure.entry.points.api.auth.mapper.AuthApiMapper;
import com.vitalitsoft.infrastructure.entry.points.api.auth.mapper.OtpApiMapper;
import com.vitalitsoft.infrastructure.entry.points.api.auth.mapper.PasswordResetApiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
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
    private final ActivateAccountUseCase activateAccountUseCase;
    private final RefreshSessionTokenUseCase refreshSessionTokenUseCase;
    private final RequestResetPasswordUseCase requestResetPasswordUseCase;
    private final ValidatePasswordResetTokenUseCase validatePasswordResetTokenUseCase;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ResendOtpUseCase resendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final ObjectValidator objectValidator;

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(objectValidator::validate)
                .map(AuthApiMapper::toCommand)
                .flatMap(loginUseCase)
                .map(AuthApiMapper::toResponseDto)
                .flatMap(token -> {
                    ResponseCookie cookie = CookieManager.createSecureCookie(Constants.REFRESH_TOKEN_COOKIE_NAME, token.getRefreshToken());
                    return ServerResponse.ok()
                            .cookie(cookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(token);
                });
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(RegisterUserRequest.class)
                .flatMap(objectValidator::validate)
                .map(AuthApiMapper::toCommand)
                .flatMap(command -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(registerUserUseCase.apply(command), Void.class)
                );
    }

    public Mono<ServerResponse> activateAccount(ServerRequest request) {
        return request.bodyToMono(ActivateAccountRequest.class)
                .flatMap(objectValidator::validate)
                .map(AuthApiMapper::toCommand)
                .flatMap(command -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(activateAccountUseCase.apply(command), Void.class)
                );
    }

    public Mono<ServerResponse> refreshToken(ServerRequest request) {
        return CookieManager.getCookieValue(request, Constants.REFRESH_TOKEN_COOKIE_NAME)
                .flatMap(refreshSessionTokenUseCase)
                .flatMap(tokenModel -> {
                    ResponseCookie cookie = CookieManager.createSecureCookie(Constants.REFRESH_TOKEN_COOKIE_NAME, tokenModel.getRefreshToken());
                    return ServerResponse.ok()
                            .cookie(cookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(AuthApiMapper.toResponseDto(tokenModel));
                });
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        ResponseCookie deleteCookie = CookieManager.deleteCookie(Constants.REFRESH_TOKEN_COOKIE_NAME);
        return CookieManager.getCookieValue(request, Constants.REFRESH_TOKEN_COOKIE_NAME)
                .flatMap(ele -> ServerResponse.ok()
                        .cookie(deleteCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(logoutUseCase.apply(ele), Void.class));
    }

    public Mono<ServerResponse> requestResetPassword(ServerRequest request) {
        return request.bodyToMono(RequestResetPassword.class)
                .flatMap(objectValidator::validate)
                .map(PasswordResetApiMapper::toCommand)
                .flatMap(command -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestResetPasswordUseCase.apply(command), Void.class)
                );
    }

    public Mono<ServerResponse> validatePasswordResetToken(ServerRequest request) {
        return request.bodyToMono(ValidateTokenResetRequest.class)
                .flatMap(objectValidator::validate)
                .map(PasswordResetApiMapper::toCommand)
                .flatMap(validatePasswordResetTokenUseCase)
                .map(PasswordResetApiMapper::toResponseDto)
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto)
                );
    }

    public Mono<ServerResponse> confirmPasswordReset(ServerRequest request) {
        return request.bodyToMono(ConfirmPasswordResetRequest.class)
                .flatMap(objectValidator::validate)
                .map(PasswordResetApiMapper::toCommand)
                .flatMap(command -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(confirmPasswordResetUseCase.apply(command), Void.class)
                );
    }

    public Mono<ServerResponse> resendOtp(ServerRequest request) {
        return request.bodyToMono(ResendOtpRequest.class)
                .flatMap(objectValidator::validate)
                .map(OtpApiMapper::toCommand)
                .flatMap(command -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(resendOtpUseCase.apply(command), Void.class));
    }

    public Mono<ServerResponse> verifyOtp(ServerRequest request) {
        return request.bodyToMono(ValidateOtpRequest.class)
                .flatMap(objectValidator::validate)
                .map(OtpApiMapper::toCommand)
                .flatMap(verifyOtpUseCase)
                .map(AuthApiMapper::toResponseDto)
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto));
    }


}
