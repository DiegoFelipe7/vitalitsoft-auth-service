package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.auth;


import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.auth.gateways.AuthRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.auth.mapper.AuthMapper;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class AuthReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        AuthModel,
        AuthEntity,
        UUID,
        AuthReactiveRepository
        > implements AuthRepository {

    private final R2dbcEntityTemplate template;

    public AuthReactiveRepositoryAdapter(AuthReactiveRepository repository, ObjectMapper mapper, R2dbcEntityTemplate template) {
        super(repository, mapper, d -> mapper.mapBuilder(d, AuthModel.AuthModelBuilder.class).build());
        this.template = template;

    }


    @Override
    public Mono<AuthModel> findByEmail(String email) {
        return repository
                .findByEmail(email)
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.USER_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .map(AuthMapper::mapToModel);

    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    @Transactional
    public Mono<AuthModel> save(AuthModel authModel) {
        return this.repository
                .save(AuthMapper.mapToEntity(authModel))
                .map(AuthMapper::toModel);
    }


    @Override
    public Mono<AuthModel> updateStatus(UUID id, Status status) {

        return template.update(AuthEntity.class)
                .matching(Query.query(Criteria.where("id").is(id)))
                .apply(Update.update("status", status).set("updated_at", LocalDateTime.now()))
                .filter(rows -> rows > 0)
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.USER_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .then(
                        template.selectOne(
                                Query.query(Criteria.where("id").is(id)),
                                AuthEntity.class
                        )
                )
                .map(AuthMapper::toModel);
    }

    @Override
    public Mono<Void> updatePassword(UUID uuid, String newPassword) {
        return template.update(AuthEntity.class)
                .matching(Query.query(Criteria.where("id").is(uuid)))
                .apply(Update.update("password", newPassword).set("updated_at", LocalDateTime.now()))
                .then();

    }

    @Override
    public Mono<AuthModel> findById(UUID id) {
        return repository
                .findById(id)
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.USER_NOT_FOUND, HttpStatus.BAD_REQUEST)))
                .map(AuthMapper::mapToModel);
    }

}
