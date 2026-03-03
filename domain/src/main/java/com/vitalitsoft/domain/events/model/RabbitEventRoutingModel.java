package com.vitalitsoft.domain.events.model;

import lombok.Builder;



@Builder(toBuilder = true)
public record RabbitEventRoutingModel(String exchange, String routingKey){}
