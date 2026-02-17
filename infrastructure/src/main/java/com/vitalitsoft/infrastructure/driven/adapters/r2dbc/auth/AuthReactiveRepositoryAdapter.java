package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.auth;


import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.auth.mapper.AuthMapper;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class AuthReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        AuthModel,
        AuthEntity,
        UUID,
        AuthReactiveRepository
        > implements AuthRepository {


    public AuthReactiveRepositoryAdapter(AuthReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.mapBuilder(d, AuthModel.AuthModelBuilder.class).build());

    }


    @Override
    public Mono<AuthModel> findByEmail(String email) {
        return repository
                .findByEmail(email)
                .map(AuthMapper::mapToModel);

    }

    @Override
    public Mono<AuthModel> save(AuthModel authModel) {
        return this.repository
                .save(AuthMapper.mapToEntity(authModel))
                .map(AuthMapper::mapToModel);
    }

}
