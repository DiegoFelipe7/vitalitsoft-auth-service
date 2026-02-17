package com.vitalitsoft.infrastructure.entry.points.api.config;

import com.vitalitsoft.application.usecase.auth.LoginUseCase;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.auth.gateways.PasswordRepository;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
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
}
