package com.vitalitsoft.infrastructure.entry.points.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI publicApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexus User service API")
                        .description("API para la gestion de usuarios del sistema Nexus " +
                                "Incluye servicios para el manejo de profesionales médicos y perfiles profesionales.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipo Nexus")
                                .email("support@nexus.com")
                                .url("https://www.nexus.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo"),
                        new Server()
                                .url("https://api.nexus.com")
                                .description("Servidor de producción")
                ));
    }
}