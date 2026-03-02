package com.vitalitsoft.application.usecase.auth;


import com.vitalitsoft.application.dto.auth.response.RefreshSessionResponse;
import com.vitalitsoft.application.mapper.auth.AuthResponseMapper;
import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.TokenModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.auth.gateways.JwtRepository;
import com.vitalitsoft.domain.refreshtoken.RefreshTokenModel;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.shared.exception.NexusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class RefreshSessionTokenUseCase implements Function<String, Mono<RefreshSessionResponse>> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRepository jwtRepository;
    private final AuthRepository authRepository;

    @Override
    public Mono<RefreshSessionResponse> apply(String refreshToken) {
        log.info("Iniciando proceso de refresh token");
        return  refreshTokenRepository.findByToken(refreshToken)
                .doOnNext(RefreshTokenModel::ensureValid)
                .flatMap(this::loadActiveUser)
                .flatMap(this::rotateSession)
                .map(AuthResponseMapper::toRefreshSessionResponse)
                .doOnSuccess(response -> log.info("Refresh successful"))
                .doOnError(e -> log.warn("Refresh failed: {}", e.getMessage()));
    }


    private Mono<AuthModel> loadActiveUser(RefreshTokenModel token) {
        return authRepository.findByEmail(token.getEmail())
                .filter(user -> user.getStatus() == Status.ACTIVE)
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.ACCOUNT_LOCKED, HttpStatus.FORBIDDEN)));
    }


    private Mono<TokenModel> rotateSession(AuthModel authModel) {
        return jwtRepository.generateToken(
                        authModel.getEmail(),
                        authModel.getRole().name(),
                        authModel.requiresTwoFactor()
                )
                .flatMap(tokens ->
                        refreshTokenRepository
                                .rotate(
                                        authModel.getEmail(),
                                        authModel.getId(),
                                        tokens.getRefreshToken()
                                )
                                .thenReturn(tokens)
                );
    }
}
