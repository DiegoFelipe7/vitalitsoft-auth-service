package com.vitalitsoft.domain.shared.events;

import com.vitalitsoft.domain.events.model.RabbitEventRoutingModel;
import com.vitalitsoft.domain.shared.enums.UserEventType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
@Builder(toBuilder = true)
public class RabbitEventCatalog {
    private static final Map<UserEventType, RabbitEventRoutingModel> ROUTING_MAP =
            Map.of(
                    UserEventType.USER_CREATED,
                    RabbitEventRoutingModel.builder()
                            .exchange("user.events")
                            .routingKey("user.registration")
                            .build(),

                    UserEventType.USER_PASSWORD_RESET,
                    RabbitEventRoutingModel.builder()
                            .exchange("user.events")
                            .routingKey("password.reset.request")
                            .build()
            );

    private RabbitEventCatalog() {
        throw new IllegalStateException("Utility class");
    }

    public static RabbitEventRoutingModel resolve(UserEventType eventType) {
        var routing = ROUTING_MAP.get(eventType);

        if (routing == null) {
            throw new IllegalStateException("No routing configured for event " + eventType);
        }

        return routing;
    }

}
