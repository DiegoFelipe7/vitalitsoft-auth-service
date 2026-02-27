package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken;


import com.vitalitsoft.domain.refreshtoken.RefreshTokenModel;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.helper.ReactiveAdapterOperations;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken.mapper.RefreshTokenMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import org.reactivecommons.utils.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;


@Repository
public class RefreshTokenReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        RefreshTokenModel,
        RefreshToken,
        UUID,
        RefreshTokenReactiveRepository
        > implements RefreshTokenRepository {


    public RefreshTokenReactiveRepositoryAdapter(RefreshTokenReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.mapBuilder(d, RefreshTokenModel.RefreshTokenModelBuilder.class).build());

    }


    @Override
    public Mono<Void> save(String email, UUID userId, String token) {
        return repository.save(RefreshTokenMapper.mapToEntity(RefreshTokenModel.builder()
                .email(email)
                .userId(userId)
                .token(token)
                .expirationTime(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build())).then();
    }

    @Override
    public Mono<RefreshTokenModel> findByToken(String token) {
        return repository.findByToken(token)
                .map(RefreshTokenMapper::mapToModel);
    }

    @Override
    public Mono<Void> revokeByEmail(String email) {
        return this.repository.findAllByEmail(email)
                .flatMap(refreshToken -> {
                    refreshToken.setRevoked(true);
                    return this.repository.save(refreshToken);
                }).then();
    }
}
