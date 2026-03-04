package com.vitalitsoft.infrastructure.driven.adapters.rabbit.catalog;

import com.vitalitsoft.domain.events.model.RabbitEventRoutingModel;
import com.vitalitsoft.domain.shared.enums.UserEventType;
import com.vitalitsoft.domain.events.gateways.RabbitEventCatalog;
import com.vitalitsoft.infrastructure.driven.adapters.rabbit.config.RabbitMqProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitEventCatalogImpl implements RabbitEventCatalog {

    private final RabbitMqProperties rabbitMqProperties;

    @Override
    public RabbitEventRoutingModel resolve(UserEventType eventType) {
        log.debug("Resolviendo configuración de routing para evento: {}", eventType);

        var eventRouting = rabbitMqProperties.getBindings();

        if (eventRouting == null || !eventRouting.containsKey(eventType.name())) {
            throw new IllegalArgumentException(
                String.format("No routing configuration found for event type: %s. " +
                    "Please add configuration in YAML under adapters.rabbitmq.event-routing.%s",
                    eventType, eventType.name())
            );
        }

        var routing = eventRouting.get(eventType.name());

        RabbitEventRoutingModel routingModel = RabbitEventRoutingModel.builder()
            .exchange(routing.getExchange())
            .routingKey(routing.getRoutingKey())
            .build();

        log.debug("Configuración resuelta para {}: exchange={}, routingKey={}", eventType, routingModel.exchange(), routingModel.routingKey());
        return routingModel;
    }

    @Override
    public boolean hasRoutingFor(UserEventType eventType) {
        var eventRouting = rabbitMqProperties.getBindings();
        return eventRouting != null && eventRouting.containsKey(eventType.name());
    }
}
