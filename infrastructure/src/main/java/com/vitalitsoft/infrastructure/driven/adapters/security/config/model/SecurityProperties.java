package com.vitalitsoft.infrastructure.driven.adapters.security.config.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.security.jwt")
public record SecurityProperties(
        String secret,
        Long expiration,
        Long refreshExpiration,
        String header,
        String prefix
) {
}
