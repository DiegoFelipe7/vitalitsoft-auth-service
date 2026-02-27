package com.vitalitsoft.infrastructure.driven.adapters.r2dbc.otp;

import com.vitalitsoft.domain.otp.OtpModel;
import com.vitalitsoft.domain.otp.gateways.OtpRepository;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.helper.ReactiveAdapterOperations;
import com.vitalitsoft.infrastructure.driven.adapters.r2dbc.otp.mapper.OtpMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class OtpReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        OtpModel,
        OtpEntity,
        UUID,
        OtpReactiveRepository
        > implements OtpRepository {


    public OtpReactiveRepositoryAdapter(OtpReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.mapBuilder(d, OtpModel.OtpModelBuilder.class).build());

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

}
