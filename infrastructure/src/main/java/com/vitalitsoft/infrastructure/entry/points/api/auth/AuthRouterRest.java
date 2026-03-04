package com.vitalitsoft.infrastructure.entry.points.api.auth;

import com.vitalitsoft.application.dto.auth.request.ActivateAccountRequest;
import com.vitalitsoft.application.dto.auth.request.LoginRequest;
import com.vitalitsoft.application.dto.auth.request.RegisterUserRequest;
import com.vitalitsoft.application.dto.auth.response.LoginResponse;
import com.vitalitsoft.application.dto.passwordReset.request.ConfirmPasswordResetRequest;
import com.vitalitsoft.application.dto.passwordReset.request.RequestResetPassword;
import com.vitalitsoft.application.dto.passwordReset.request.ValidateTokenResetRequest;
import com.vitalitsoft.application.dto.otp.request.ResendOtpRequest;
import com.vitalitsoft.application.dto.otp.request.ValidateOtpRequest;
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
                            description = "Autentica a un usuario y retorna el token de acceso. El refresh token también se retorna como cookie HttpOnly.",
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
                                    @ApiResponse(responseCode = "200", description = "Login exitoso. El refresh token también se retorna como cookie HttpOnly llamada 'refreshToken'",
                                            headers = {@io.swagger.v3.oas.annotations.headers.Header(name = "Set-Cookie", description = "Refresh token como cookie HttpOnly, ejemplo: refreshToken=...; HttpOnly; Secure; Path=/; SameSite=Strict")},
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = LoginResponse.class)
                                            )
                                    ),
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
                                    @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente",
                                            content = @Content(schema = @Schema(type = "string", example = "Usuario registrado"))),
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
                                    @ApiResponse(responseCode = "200", description = "Solicitud procesada",
                                            content = @Content(schema = @Schema(type = "string", example = "Solicitud procesada"))),
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
                                    @ApiResponse(responseCode = "200", description = "Token válido",
                                            content = @Content(schema = @Schema(type = "boolean", example = "true"))),
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
                                    @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente",
                                            content = @Content(schema = @Schema(type = "string", example = "Contraseña restablecida"))),
                                    @ApiResponse(responseCode = "400", description = "Token inválido o expirado"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/resend-otp",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "resendOtp",
                    operation = @Operation(
                            operationId = "resendOtp",
                            summary = "Renviar OTP",
                            description = "Envía un código OTP al usuario por el canal especificado (email, SMS, etc). Requiere que el usuario esté registrado y autenticado.",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Datos para enviar OTP (usuario, canal, tipo de operación)",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = ResendOtpRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "OTP enviado exitosamente",
                                            content = @Content(schema = @Schema(type = "string", example = "OTP enviado"))),
                                    @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(type = "string", example = "Datos faltantes o incorrectos"))),
                                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content(schema = @Schema(type = "string", example = "Usuario no autenticado"))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(type = "string", example = "Error al enviar OTP")))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/verify-otp",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "verifyOtp",
                    operation = @Operation(
                            operationId = "verifyOtp",
                            summary = "Validar OTP",
                            description = "Valida el código OTP enviado al usuario para completar una operación segura (login, registro, cambio de contraseña, etc).",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Datos para validar OTP (usuario, código, tipo de operación)",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = ValidateOtpRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "OTP válido",
                                            content = @Content(schema = @Schema(implementation = com.vitalitsoft.application.dto.auth.response.LoginResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "OTP inválido o expirado", content = @Content(schema = @Schema(type = "string", example = "OTP incorrecto o expirado"))),
                                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content(schema = @Schema(type = "string", example = "Usuario no autenticado"))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(type = "string", example = "Error al validar OTP")))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/activate",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "activateAccount",
                    operation = @Operation(
                            operationId = "activateAccount",
                            summary = "Activar cuenta",
                            description = "Activa la cuenta de usuario con el token recibido por email.",
                            tags = {"Auth"},
                            requestBody = @RequestBody(
                                    description = "Datos para activar la cuenta (email, token)",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = ActivateAccountRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Cuenta activada exitosamente",
                                            content = @Content(schema = @Schema(type = "string", example = "Cuenta activada"))),
                                    @ApiResponse(responseCode = "400", description = "Token inválido o expirado"),
                                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/refresh-token",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "refreshToken",
                    operation = @Operation(
                            operationId = "refreshToken",
                            summary = "Refrescar token de acceso",
                            description = "Genera un nuevo token de acceso usando el refresh token enviado como cookie HttpOnly (refreshToken).",
                            tags = {"Auth"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Nuevo token de acceso generado",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = LoginResponse.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado"),
                                    @ApiResponse(responseCode = "400", description = "Refresh token no enviado en la cookie"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/auth/logout",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "logout",
                    operation = @Operation(
                            operationId = "logout",
                            summary = "Cerrar sesión",
                            description = "Elimina la cookie HttpOnly de refresh token y cierra la sesión del usuario.",
                            tags = {"Auth"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Logout exitoso. La cookie refreshToken es eliminada.",
                                            headers = {@io.swagger.v3.oas.annotations.headers.Header(name = "Set-Cookie", description = "Cookie refreshToken eliminada, ejemplo: refreshToken=; HttpOnly; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT")},
                                            content = @Content(schema = @Schema(type = "string", example = "Logout exitoso"))),
                                    @ApiResponse(responseCode = "401", description = "No autorizado"),
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
                .POST("/auth/activate", handler::activateAccount)
                .POST("/auth/logout", handler::logout)
                .POST("/auth/refresh-token", handler::refreshToken)
                .POST("/auth/request-reset-password", handler::requestResetPassword)
                .POST("/auth/validate-reset-token", handler::validatePasswordResetToken)
                .POST("/auth/confirm-reset-password", handler::confirmPasswordReset)
                .POST("/auth/resend-otp", handler::resendOtp)
                .POST("/auth/verify-otp", handler::verifyOtp)
                .build();
    }

}
