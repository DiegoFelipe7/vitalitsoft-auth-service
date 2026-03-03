package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken;


import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.helper.ReactiveAdapterOperations;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken.mapper.UserTokenMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Repository
public class UserTokenReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        UserTokenModel,
        UserToken,
        UUID,
        UserTokenReactiveRepository
        > implements UserTokenRepository {


    public UserTokenReactiveRepositoryAdapter(UserTokenReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.mapBuilder(d, UserTokenModel.UserTokenModelBuilder.class).build());

    }


    @Override
    @Transactional
    public Mono<UserTokenModel> save(UserTokenModel userTokenModel) {
        return this.repository.save(UserTokenMapper.mapToEntity(userTokenModel))
                .map(UserTokenMapper::mapToModel);
    }

    @Override
    public Mono<UserTokenModel> findByTokenAndType(String token, TokenType tokenType) {
        return this.repository.findByTokenAndTokenType(token, tokenType)
                .filter(ele-> !Boolean.TRUE.equals(ele.getUsed()))
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.TOKEN_EXPIRED_OR_INVALID, HttpStatus.UNAUTHORIZED)))
                .map(UserTokenMapper::mapToModel);
    }

    @Override
    public Mono<Void> markAsUsed(UserTokenModel userToken) {
        UserTokenModel usedToken = userToken.withId(userToken.getId()).withUsed(true);
        return this.repository
                .save(UserTokenMapper.mapToEntity(usedToken))
                .then();
    }

}
