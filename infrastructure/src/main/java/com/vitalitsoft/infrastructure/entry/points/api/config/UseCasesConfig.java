package com.vitalitsoft.infrastructure.entry.points.api.config;

import com.vitalitsoft.application.usecase.auth.LoginUseCase;
import com.vitalitsoft.application.usecase.auth.RegisterUserUseCase;
import com.vitalitsoft.application.usecase.passwordReset.ConfirmPasswordResetUseCase;
import com.vitalitsoft.application.usecase.passwordReset.RequestResetPasswordUseCase;
import com.vitalitsoft.application.usecase.passwordReset.ValidatePasswordResetTokenUseCase;
import com.vitalitsoft.application.usecase.otp.SendOtpUseCase;
import com.vitalitsoft.application.usecase.otp.ValidateOtpUseCase;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.auth.gateways.PasswordRepository;
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
    public LoginUseCase loginUseCase(
            AuthRepository authRepository,
            PasswordRepository passwordRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtRepository jwtRepository
    ) {
        return new LoginUseCase(authRepository, passwordRepository, refreshTokenRepository, jwtRepository);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            AuthRepository authRepository,
            PasswordRepository passwordRepository,
            EventsRepository<UserRegisterEventModel> eventsRepository
    ) {
        return new RegisterUserUseCase(authRepository, passwordRepository, eventsRepository);
    }

    @Bean
    public RequestResetPasswordUseCase requestResetPasswordUseCase(
            AuthRepository authRepository,
            UserTokenRepository userTokenRepository,
            EventsRepository<String> eventsRepository
    ) {
        return new RequestResetPasswordUseCase(authRepository, userTokenRepository, eventsRepository);
    }

    @Bean
    public ValidatePasswordResetTokenUseCase validatePasswordResetTokenUseCase(
            UserTokenRepository userTokenRepository
    ) {
        return new ValidatePasswordResetTokenUseCase(userTokenRepository);
    }

    @Bean
    public ConfirmPasswordResetUseCase confirmPasswordResetUseCase(
            UserTokenRepository userTokenRepository,
            AuthRepository authRepository
    ) {
        return new ConfirmPasswordResetUseCase(userTokenRepository, authRepository);
    }

    @Bean
    public SendOtpUseCase sendOtpUseCase(OtpRepository otpRepository) {
        return new SendOtpUseCase(otpRepository);
    }

    @Bean
    public ValidateOtpUseCase validateOtpUseCase(OtpRepository otpRepository) {
        return new ValidateOtpUseCase(otpRepository);
    }
}
