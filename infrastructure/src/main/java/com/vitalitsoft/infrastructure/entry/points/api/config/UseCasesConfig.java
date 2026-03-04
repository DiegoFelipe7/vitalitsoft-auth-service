package com.vitalitsoft.infrastructure.entry.points.api.config;

import com.vitalitsoft.application.usecase.auth.*;
import com.vitalitsoft.application.usecase.passwordReset.ConfirmPasswordResetUseCase;
import com.vitalitsoft.application.usecase.passwordReset.RequestResetPasswordUseCase;
import com.vitalitsoft.application.usecase.passwordReset.ValidatePasswordResetTokenUseCase;
import com.vitalitsoft.application.usecase.otp.ResendOtpUseCase;
import com.vitalitsoft.application.usecase.otp.VerifyOtpUseCase;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.events.model.PasswordResetEventModel;
import com.vitalitsoft.domain.events.model.SendOtpEventModel;
import com.vitalitsoft.domain.hashing.HashingRepository;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.events.model.UserRegisterEventModel;
import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public LoginUseCase loginUseCase(AuthRepository authRepository, HashingRepository hashingRepository, RefreshTokenRepository refreshTokenRepository, JwtRepository jwtRepository, OtpRepository otpRepository, EventsRepository<SendOtpEventModel> eventPublisher) {
        return new LoginUseCase(authRepository, hashingRepository, refreshTokenRepository, jwtRepository, otpRepository, eventPublisher);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(AuthRepository authRepository, HashingRepository hashingRepository, EventsRepository<UserRegisterEventModel> eventsRepository, UserTokenRepository userTokenRepository) {
        return new RegisterUserUseCase(authRepository, hashingRepository, eventsRepository, userTokenRepository);
    }

    @Bean
    public ActivateAccountUseCase activateAccountUseCase(AuthRepository authRepository, UserTokenRepository userTokenRepository) {
        return new ActivateAccountUseCase(authRepository, userTokenRepository);
    }

    @Bean
    public LogoutUseCase logoutUseCase(RefreshTokenRepository refreshTokenRepository, JwtRepository jwtRepository) {
        return new LogoutUseCase(refreshTokenRepository, jwtRepository);
    }

    @Bean
    public RefreshSessionTokenUseCase refreshSessionTokenUseCase(RefreshTokenRepository refreshTokenRepository, JwtRepository jwtRepository, AuthRepository authRepository) {
        return new RefreshSessionTokenUseCase(refreshTokenRepository, jwtRepository, authRepository);
    }

    @Bean
    public RequestResetPasswordUseCase requestResetPasswordUseCase(AuthRepository authRepository, UserTokenRepository userTokenRepository, EventsRepository<PasswordResetEventModel> eventsRepository) {
        return new RequestResetPasswordUseCase(authRepository, userTokenRepository, eventsRepository);
    }

    @Bean
    public ValidatePasswordResetTokenUseCase validatePasswordResetTokenUseCase(UserTokenRepository userTokenRepository) {
        return new ValidatePasswordResetTokenUseCase(userTokenRepository);
    }

    @Bean
    public ConfirmPasswordResetUseCase confirmPasswordResetUseCase(UserTokenRepository userTokenRepository, AuthRepository authRepository, HashingRepository hashingRepository) {
        return new ConfirmPasswordResetUseCase(userTokenRepository, authRepository, hashingRepository);
    }

    @Bean
    public ResendOtpUseCase sendOtpUseCase(AuthRepository authRepository, OtpRepository otpRepository, EventsRepository<SendOtpEventModel> eventPublisher, HashingRepository hashingRepository) {
        return new ResendOtpUseCase(authRepository, otpRepository, eventPublisher, hashingRepository);
    }

    @Bean
    public VerifyOtpUseCase validateOtpUseCase(OtpRepository otpRepository, AuthRepository authRepository, HashingRepository hashingRepository, JwtRepository jwtRepository, RefreshTokenRepository refreshTokenRepository) {
        return new VerifyOtpUseCase(otpRepository, authRepository, hashingRepository, jwtRepository, refreshTokenRepository);
    }
}
