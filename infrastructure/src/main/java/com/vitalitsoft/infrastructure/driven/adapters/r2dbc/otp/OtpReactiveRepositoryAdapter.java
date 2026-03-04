package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.otp;

import com.vitalitsoft.domain.otp.OtpModel;
import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.auth.AuthEntity;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.auth.mapper.AuthMapper;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.helper.ReactiveAdapterOperations;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.otp.mapper.OtpMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class OtpReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        OtpModel,
        OtpEntity,
        UUID,
        OtpReactiveRepository
        > implements OtpRepository {

    private final R2dbcEntityTemplate template;

    public OtpReactiveRepositoryAdapter(OtpReactiveRepository repository, ObjectMapper mapper, R2dbcEntityTemplate template) {
        super(repository, mapper, d -> mapper.mapBuilder(d, OtpModel.OtpModelBuilder.class).build());
        this.template = template;

    }


    @Override
    public Mono<OtpModel> save(OtpModel otpModel) {
        return repository.save(OtpMapper.toEntity(otpModel))
                .map(OtpMapper::toModel);
    }

    @Override
    public Mono<OtpModel> findBySessionId(String sessionId) {
        return repository
                .findBySessionId(sessionId)
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.SESSION_ID_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .map(OtpMapper::toModel);
    }

    @Override
    public Mono<OtpModel> markAsUsed(UUID uuid) {
        return template.update(OtpEntity.class)
                .matching(Query.query(Criteria.where("id").is(uuid)))
                .apply(Update.update("used", true).set("updated_at", LocalDateTime.now()))
                .filter(rows -> rows > 0)
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.OTP_INVALID, HttpStatus.NOT_FOUND)))
                .then(
                        template.selectOne(
                                Query.query(Criteria.where("id").is(uuid)),
                                OtpEntity.class
                        )
                )
                .map(OtpMapper::toModel);
    }

    @Override
    public Mono<OtpModel> update(OtpModel model) {
        OtpEntity entity = OtpMapper.toEntity(model);
        return template.update(entity)
                .switchIfEmpty(Mono.error(
                        new NexusException(
                                NexusException.Type.OTP_INVALID,
                                HttpStatus.NOT_FOUND
                        )
                ))
                .map(OtpMapper::toModel);
    }

}
