package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken;


import com.vitalitsoft.domain.refreshtoken.RefreshTokenModel;
import com.vitalitsoft.domain.refreshtoken.gateways.RefreshTokenRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.helper.ReactiveAdapterOperations;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.refreshToken.mapper.RefreshTokenMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import org.reactivecommons.utils.ObjectMapper;

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
    public Mono<RefreshTokenModel> findByToken(String token) {
        return repository.findByToken(token)
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.TOKEN_NOT_FOUND, HttpStatus.UNAUTHORIZED)))
                .map(RefreshTokenMapper::mapToModel);
    }

    @Override
    public Mono<Void> rotate(String email, UUID userId, String token) {

        return this.revokeTokenByUserId(userId)
                .then(Mono.defer(() -> this.repository.save(RefreshTokenMapper.toEntity(userId, email, token)))).then();
    }

    @Override
    public Mono<Void> revokeTokenByUserId(UUID userId) {
        return this.repository.findAllByUserId(userId)
                .flatMap(refreshToken -> {
                    refreshToken.revoke();
                    return this.repository.save(refreshToken);
                })
                .then();
    }
}
