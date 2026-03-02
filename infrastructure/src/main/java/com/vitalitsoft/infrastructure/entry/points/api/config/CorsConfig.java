package com.vitalitsoft.infrastructure.entry.points.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

    @Bean
    CorsWebFilter corsWebFilter(@Value("${cors.allowed-origins}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        List<String> allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());
        config.setAllowedOrigins(allowedOrigins);

        config.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "OPTIONS"));

        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "X-XSRF-TOKEN"
        ));

        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Total-Count"
        ));

        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
