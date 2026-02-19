package com.vitalitsoft.infrastructure.entry.points.api.auth;

import com.vitalitsoft.application.dto.auth.LoginRequest;
import com.vitalitsoft.application.dto.auth.RegisterUserRequest;
import com.vitalitsoft.application.dto.passwordReset.ConfirmPasswordResetRequest;
import com.vitalitsoft.application.dto.passwordReset.RequestResetPassword;
import com.vitalitsoft.application.dto.passwordReset.ValidateTokenResetRequest;
import com.vitalitsoft.application.dto.otp.SendOtpRequest;
import com.vitalitsoft.application.dto.otp.ValidateOtpRequest;
import io.swagger.v3.oas.annotations.Operation;
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

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthRouterRest {
    @RouterOperations({
            @RouterOperation(
                    path = "/auth/login",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "login",
                    operation = @Operation(
                            operationId = "login",
                            summary = "Iniciar sesión",
                            description = "Autentica a un usuario y retorna el token de acceso.",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Credenciales de usuario",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LoginRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Login exitoso"),
                                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/register",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "register",
                    operation = @Operation(
                            operationId = "register",
                            summary = "Registrar usuario",
                            description = "Registra un nuevo usuario en el sistema.",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Datos de registro de usuario",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = RegisterUserRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
                                    @ApiResponse(responseCode = "409", description = "Email ya registrado"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/request-reset-password",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "requestResetPassword",
                    operation = @Operation(
                            operationId = "requestResetPassword",
                            summary = "Solicitar reseteo de contraseña",
                            description = "Solicita el envío de un token para restablecer la contraseña.",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Email del usuario para resetear contraseña",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = RequestResetPassword.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Solicitud procesada"),
                                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/validate-reset-token",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "validatePasswordResetToken",
                    operation = @Operation(
                            operationId = "validatePasswordResetToken",
                            summary = "Validar token de reseteo de contraseña",
                            description = "Valida si el token de reseteo de contraseña es válido y no ha expirado.",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Token a validar",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = ValidateTokenResetRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Token válido"),
                                    @ApiResponse(responseCode = "400", description = "Token inválido o expirado"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/confirm-reset-password",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "confirmPasswordReset",
                    operation = @Operation(
                            operationId = "confirmPasswordReset",
                            summary = "Confirmar reseteo de contraseña",
                            description = "Confirma el cambio de contraseña usando el token recibido.",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Datos para confirmar el reseteo de contraseña",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = ConfirmPasswordResetRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
                                    @ApiResponse(responseCode = "400", description = "Token inválido o expirado"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/send-otp",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "sendOtp",
                    operation = @Operation(
                            operationId = "sendOtp",
                            summary = "Enviar OTP",
                            description = "Envía un código OTP al usuario por el canal especificado.",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Datos para enviar OTP",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = SendOtpRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "OTP enviado exitosamente"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/validate-otp",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "validateOtp",
                    operation = @Operation(
                            operationId = "validateOtp",
                            summary = "Validar OTP",
                            description = "Valida el código OTP enviado al usuario.",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Datos para validar OTP",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = ValidateOtpRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "OTP válido o inválido"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            )
    })

    @Bean
    public RouterFunction<ServerResponse> authRoutes(AuthHandler handler) {
        return route()
                .POST("/auth/login", handler::login)
                .POST("/auth/register", handler::register)
                .POST("/auth/request-reset-password", handler::requestResetPassword)
                .POST("/auth/validate-reset-token", handler::validatePasswordResetToken)
                .POST("/auth/confirm-reset-password", handler::confirmPasswordReset)
                .POST("/auth/send-otp", handler::sendOtp)
                .POST("/auth/validate-otp", handler::validateOtp)
                .build();
    }

}
