package com.vitalitsoft.infrastructure.entry.points.api.user;

import co.com.nexus.api.user.dto.UpdateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouterRest {

        @Bean
        @RouterOperations({
                @RouterOperation(
                        path = "/users",
                        produces = {"application/json"},
                        method = RequestMethod.GET,
                        beanClass = UserHandler.class,
                        beanMethod = "getAllUsers",
                        operation = @Operation(
                                operationId = "getAllUsers",
                                summary = "Obtener todos los usuarios",
                                description = "Retorna el listado completo de usuarios registrados en el sistema.",
                                tags = {"Usuario"},
                                responses = {
                                        @ApiResponse(
                                                responseCode = "200",
                                                description = "Listado de usuarios retornado correctamente"
                                        ),
                                        @ApiResponse(
                                                responseCode = "500",
                                                description = "Error interno del servidor"
                                        )
                                }
                        )
                ),
                @RouterOperation(
                        path = "/users/{id}",
                        produces = {"application/json"},
                        method = RequestMethod.GET,
                        beanClass = UserHandler.class,
                        beanMethod = "getUserById",
                        operation = @Operation(
                                operationId = "getUserById",
                                summary = "Obtener usuario por ID",
                                description = "Retorna la información de un usuario específico por su identificador único.",
                                tags = {"Usuario"},
                                parameters = {
                                        @Parameter(
                                                name = "id",
                                                description = "ID único del usuario",
                                                required = true,
                                                in = ParameterIn.PATH,
                                                schema = @Schema(type = "string")
                                        )
                                },
                                responses = {
                                        @ApiResponse(
                                                responseCode = "200",
                                                description = "Usuario encontrado"
                                        ),
                                        @ApiResponse(
                                                responseCode = "404",
                                                description = "Usuario no encontrado"
                                        ),
                                        @ApiResponse(
                                                responseCode = "500",
                                                description = "Error interno del servidor"
                                        )
                                }
                        )
                ),
                @RouterOperation(
                        path = "/users/{id}",
                        produces = {"application/json"},
                        method = RequestMethod.PATCH,
                        beanClass = UserHandler.class,
                        beanMethod = "updateUser",
                        operation = @Operation(
                                operationId = "updateUser",
                                summary = "Actualizar usuario por ID",
                                description = "Actualiza los datos de un usuario existente.",
                                tags = {"Usuario"},
                                parameters = {
                                        @Parameter(
                                                name = "id",
                                                description = "ID único del usuario a actualizar",
                                                required = true,
                                                in = ParameterIn.PATH,
                                                schema = @Schema(type = "string")
                                        )
                                },
                                requestBody = @RequestBody(
                                        description = "Datos a actualizar del usuario",
                                        required = true,
                                        content = @Content(
                                                mediaType = "application/json",
                                                schema = @Schema(implementation = UpdateUserRequest.class)
                                        )
                                ),
                                responses = {
                                        @ApiResponse(
                                                responseCode = "200",
                                                description = "Usuario actualizado exitosamente"
                                        ),
                                        @ApiResponse(
                                                responseCode = "404",
                                                description = "Usuario no encontrado"
                                        ),
                                        @ApiResponse(
                                                responseCode = "400",
                                                description = "Datos de entrada inválidos"
                                        ),
                                        @ApiResponse(
                                                responseCode = "500",
                                                description = "Error interno del servidor"
                                        )
                                }
                        )
                ),
                @RouterOperation(
                        path = "/users/{id}/avatar",
                        produces = {"application/json"},
                        method = RequestMethod.PATCH,
                        beanClass = UserHandler.class,
                        beanMethod = "updateUserAvatar",
                        operation = @Operation(
                                operationId = "updateUserAvatar",
                                summary = "Actualizar avatar de usuario",
                                description = "Actualiza la imagen de avatar del usuario.",
                                tags = {"Usuario"},
                                parameters = {
                                        @Parameter(
                                                name = "id",
                                                description = "ID único del usuario a actualizar",
                                                required = true,
                                                in = ParameterIn.PATH,
                                                schema = @Schema(type = "string")
                                        )
                                },
                                requestBody = @RequestBody(
                                        description = "Archivo de imagen del avatar (base64 o URL)",
                                        required = true
                                ),
                                responses = {
                                        @ApiResponse(
                                                responseCode = "200",
                                                description = "Avatar actualizado exitosamente"
                                        ),
                                        @ApiResponse(
                                                responseCode = "404",
                                                description = "Usuario no encontrado"
                                        ),
                                        @ApiResponse(
                                                responseCode = "400",
                                                description = "Datos de entrada inválidos"
                                        ),
                                        @ApiResponse(
                                                responseCode = "500",
                                                description = "Error interno del servidor"
                                        )
                                }
                        )
                )
        })
    public RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
        return route(GET("users"), handler::getAllUsers)
                .and(route(GET("users/{id}"), handler::getUserById))
                .and(route(PATCH("users/{id}"), handler::updateUser))
                .and(route(PATCH("users/{id}/avatar"), handler::updateUserAvatar));
    }
}
