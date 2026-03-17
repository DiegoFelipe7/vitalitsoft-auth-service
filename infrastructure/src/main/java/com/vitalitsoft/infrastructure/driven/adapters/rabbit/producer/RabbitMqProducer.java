package com.vitalitsoft.infrastructure.driven.adapters.rabbit.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalitsoft.domain.events.gateways.EventsRepository;
import com.vitalitsoft.domain.shared.exception.VitalitSoftException;
import com.vitalitsoft.infrastructure.driven.adapters.rabbit.config.RabbitMqProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqProducer<T> implements EventsRepository<T> {
    private final Sender sender;
    private final ObjectMapper objectMapper;
    private final RabbitMqProperties rabbitMqProperties;
    private Map<String, RabbitMqProperties.QueueBinding> bindingsIndex;

    @PostConstruct
    void init() {
        bindingsIndex = rabbitMqProperties.getBindings()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        RabbitMqProperties.QueueBinding::getKey,
                        Function.identity()
                ));

        log.info("RabbitMqProducer initialized with {} bindings", bindingsIndex.size());
    }

    @Override
    public Mono<Void> publish(String key, T event) {

        var binding = resolveBinding(key);
        var payload = convertToJson(event);

        OutboundMessage message = new OutboundMessage(
                binding.getExchange(),
                binding.getRoutingKey(),
                payload.getBytes(StandardCharsets.UTF_8)
        );

        return sender.send(Mono.just(message))
                .doOnSuccess(unused ->
                        log.info("Event published → exchange={}, routingKey={}",
                                binding.getExchange(),
                                binding.getRoutingKey()))
                .doOnError(error ->
                        log.error("Error publishing event with key {}: {}", key, error.getMessage()))
                .then();
    }


    private RabbitMqProperties.QueueBinding resolveBinding(String key) {
        var binding = bindingsIndex.get(key);

        if (binding == null) {
            throw new VitalitSoftException(VitalitSoftException.Type.INTERNAL_ERROR);
        }

        return binding;
    }



    private String convertToJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Error converting event to JSON: {}", e.getMessage());
            throw new VitalitSoftException("Error converting event to JSON");
        }
    }
}
