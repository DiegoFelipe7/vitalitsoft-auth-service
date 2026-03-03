package com.vitalitsoft.domain.events.gateways;

import reactor.core.publisher.Mono;

public interface EventsRepository<T> {
    Mono<Void> publish(String exchange, String routingKey, T event);
}
