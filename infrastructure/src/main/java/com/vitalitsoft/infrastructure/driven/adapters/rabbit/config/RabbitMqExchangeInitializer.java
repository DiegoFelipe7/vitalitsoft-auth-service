package com.vitalitsoft.infrastructure.driven.adapters.rabbit.config;

import com.rabbitmq.client.AMQP;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ExchangeSpecification;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqExchangeInitializer {

    private final Sender sender;
    private final RabbitMqProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRabbitMq() {
        declareExchanges()
                .then(declareQueues())
                //.then(createBindings())
                .doOnSuccess(unused -> log.info("RabbitMQ infrastructure initialized successfully"))
                .doOnError(error -> log.error("Error initializing RabbitMQ infrastructure: {}", error.getMessage()))
                .subscribe();
    }

    private Mono<Void> declareExchanges() {
        if (properties.getExchanges() == null || properties.getExchanges().isEmpty()) {
            log.warn("No exchanges configured for initialization");
            return Mono.empty();
        }

        return Flux.fromIterable(properties.getExchanges())
                .flatMap(this::declareExchange)
                .then()
                .doOnSuccess(unused -> log.info("All exchanges declared successfully"));
    }

    private Mono<AMQP.Exchange.DeclareOk> declareExchange(String exchangeName) {
        return sender.declareExchange(
                        ExchangeSpecification.exchange(exchangeName)
                                .durable(true)
                                .type("topic")
                )
                .doOnSuccess(unused -> log.info("Exchange '{}' declared", exchangeName))
                .doOnError(error -> log.error("Failed to declare exchange '{}': {}", exchangeName, error.getMessage()));
    }

    private Mono<Void> declareQueues() {
        if (properties.getQueues() == null || properties.getQueues().isEmpty()) {
            log.warn("No queues configured for initialization");
            return Mono.empty();
        }

        return Flux.fromIterable(properties.getQueues())
                .flatMap(this::declareQueue)
                .then()
                .doOnSuccess(unused -> log.info("All queues declared successfully"));
    }

    private Mono<AMQP.Queue.DeclareOk> declareQueue(String queueName) {
        return sender.declareQueue(
                        QueueSpecification.queue(queueName)
                                .durable(true)
                                .autoDelete(false)
                )
                .doOnSuccess(unused -> log.info("Queue '{}' declared", queueName))
                .doOnError(error -> log.error("Failed to declare queue '{}': {}", queueName, error.getMessage()));
    }

    private Mono<Void> createBindings() {
        if (properties.getBindings() == null || properties.getBindings().isEmpty()) {
            log.warn("No bindings configured for initialization");
            return Mono.empty();
        }
       /* return Flux.fromIterable(Arrays.asList(properties.getBindings().entrySet().toArray()))
                .flatMap(this::createBinding)
                .then()
                .doOnSuccess(unused -> log.info("All bindings created successfully"));*/
        return Mono.empty().then();
    }

    private Mono<AMQP.Queue.BindOk> createBinding(RabbitMqProperties.QueueBinding binding) {
        return sender.bind(
                        BindingSpecification.binding()
                                .exchange(binding.getExchange())
                                .queue(binding.getQueue())
                                .routingKey(binding.getRoutingKey())
                )
                .doOnSuccess(unused -> log.info("Binding created: queue '{}' -> exchange '{}' with routing key '{}'", 
                        binding.getQueue(), binding.getExchange(), binding.getRoutingKey()))
                .doOnError(error -> log.error("Failed to create binding: {}", error.getMessage()));
    }
}
