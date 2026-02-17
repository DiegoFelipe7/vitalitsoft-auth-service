package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken;


import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.helper.ReactiveAdapterOperations;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken.mapper.UserTokenMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
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
    public Mono<UserTokenModel> saveRequest(UserTokenModel userTokenModel) {
        return this.repository.save(UserTokenMapper.mapToEntity(userTokenModel))
                .map(UserTokenMapper::mapToModel);
    }

    @Override
    public Mono<UserTokenModel> findByTokenAndType(String token, TokenType tokenType) {
        return this.repository.findByTokenAndTokenType(token, tokenType)
                .map(UserTokenMapper::mapToModel);
    }

    @Override
    public Mono<Void> markAsUsed(UserTokenModel userToken) {
        userToken.setUsed(true);
        return this.repository
                .save(UserTokenMapper.mapToEntity(userToken))
                .then();
    }

}
