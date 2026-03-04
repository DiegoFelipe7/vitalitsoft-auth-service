package com.vitalitsoft.domain.events.gateways;

import com.vitalitsoft.domain.events.model.RabbitEventRoutingModel;
import com.vitalitsoft.domain.shared.enums.UserEventType;

public interface RabbitEventCatalog {
    RabbitEventRoutingModel resolve(UserEventType eventType);
    boolean hasRoutingFor(UserEventType eventType);

}
