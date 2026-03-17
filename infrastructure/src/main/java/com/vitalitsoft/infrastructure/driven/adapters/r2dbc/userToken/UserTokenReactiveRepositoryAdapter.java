package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken;


import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.shared.exception.VitalitSoftException;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import com.vitalitsoft.domain.userToken.gateways.UserTokenRepository;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.helper.ReactiveAdapterOperations;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.userToken.mapper.UserTokenMapper;
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
public class UserTokenReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        UserTokenModel,
        UserToken,
        UUID,
        UserTokenReactiveRepository
        > implements UserTokenRepository {

    private final R2dbcEntityTemplate template;

    public UserTokenReactiveRepositoryAdapter(UserTokenReactiveRepository repository, ObjectMapper mapper, R2dbcEntityTemplate template) {
        super(repository, mapper, d -> mapper.mapBuilder(d, UserTokenModel.UserTokenModelBuilder.class).build());
        this.template = template;

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
                .filter(ele -> !Boolean.TRUE.equals(ele.getUsed()))
                .switchIfEmpty(Mono.error(new VitalitSoftException(VitalitSoftException.Type.TOKEN_EXPIRED_OR_INVALID)))
                .map(UserTokenMapper::mapToModel);
    }

    @Override
    @Transactional
    public Mono<Void> markAsUsed(UUID uuid) {
        return this.template.update(UserToken.class)
                .matching(Query.query(Criteria.where("id").is(uuid)))
                .apply(Update.update("used", true).set("updated_at", LocalDateTime.now()))
                .then();
    }

}
