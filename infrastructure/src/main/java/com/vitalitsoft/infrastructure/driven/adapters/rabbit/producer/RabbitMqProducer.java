package com.vitalitsoft.infrastructure.driven.adapters.rabbit.producer;

import com.vitalitsoft.domain.events.gateways.EventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqProducer<T> implements EventsRepository<T> {


    @Override
    public Mono<Void> publish(String exchange, String routingKey, T event) {
        return Mono.just(true).then();
    }
}
