package co.com.bancolombia.security.mock.events.gateways;

import reactor.core.publisher.Mono;

public interface EventsRepository<T> {
    Mono<Void> publish(String exchange, String routingKey, T event);
}
