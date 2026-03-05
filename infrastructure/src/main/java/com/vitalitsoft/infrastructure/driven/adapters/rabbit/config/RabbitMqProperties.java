package com.vitalitsoft.infrastructure.driven.adapters.rabbit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "adapters.rabbitmq")
public class RabbitMqProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String virtualHost;
    private Integer connectionTimeout;
    private Set<String> exchanges;
    private Set<String> queues;
    private Set<String> routingKeys;
    private List<QueueBinding> bindings;

    @Getter
    @Setter
    public static class QueueBinding {
        private String key;
        private String exchange;
        private String routingKey;
        private String queue;
    }

}
