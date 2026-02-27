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
                        .title("Vitalitsoft Auth Service API")
                        .description("API para la gestión de autenticación y autorización del sistema Vitalitsoft. Incluye servicios para login, registro, validación de OTP y gestión de tokens JWT.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipo Vitalitsoft")
                                .email("support@vitalitsoft.com")
                                .url("https://www.vitalitsoft.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servidor de desarrollo"),
                        new Server()
                                .url("https://api.vitalitsoft.com")
                                .description("Servidor de producción")
                ));
    }
}