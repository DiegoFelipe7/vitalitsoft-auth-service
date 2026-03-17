package com.vitalitsoft.infrastructure.entry.points.api.shared.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalitsoft.domain.shared.exception.VitalitSoftException;
import com.vitalitsoft.infrastructure.entry.points.api.shared.utilities.ExceptionUtils;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import io.r2dbc.spi.R2dbcBadGrammarException;

@Slf4j
@Configuration
@Order(-2)
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ErrorResponse errorResponse = buildErrorResponse(ex);
        HttpStatus status = errorResponse.status();

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer dataBuffer;

        try {
            dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse));
        } catch (Exception e) {
            log.error("Error al serializar la respuesta de error", e);
            dataBuffer = bufferFactory.wrap("Error interno del servidor".getBytes());
        }

        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    private ErrorResponse buildErrorResponse(Throwable ex) {
        if (ex instanceof VitalitSoftException customException) {
            return handleCustomException(customException);
        } else if (ex instanceof WebExchangeBindException validationException) {
            return handleValidationException(validationException);
        } else if (ex instanceof InvalidFormatException invalidFormatException) {
            return handleInvalidFormatException(invalidFormatException);
        } else if (ex instanceof R2dbcDataIntegrityViolationException integrityException) {
            return handleR2dbcDataIntegrityViolationException(integrityException);
        } else if (ex instanceof R2dbcBadGrammarException badGrammarException) {
            return handleR2dbcBadGrammarException(badGrammarException);
        } else if (ex instanceof Exception exception) {
            return handleIllegalArgumentException(exception);
        } else {
            return handleGenericException(ex);
        }
    }

    private ErrorResponse handleInvalidFormatException(InvalidFormatException ex) {
        String details = ex.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .collect(Collectors.joining(", ")) + ": Valor inválido " + ex.getValue();

        return ErrorResponse.of(details, HttpStatus.BAD_REQUEST, LocalDateTime.now(), Arrays.toString(ex.getStackTrace()));
    }

    private ErrorResponse handleCustomException(VitalitSoftException ex) {
        HttpStatus status = mapToHttpStatus(ex.getType());
        return ErrorResponse.of(ex.getMessage(), status, LocalDateTime.now(), ExceptionUtils.origin(ex) + ": " + ExceptionUtils.rootCause(ex));
    }

    private ErrorResponse handleValidationException(WebExchangeBindException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining(", "));

        return ErrorResponse.of(details, HttpStatus.BAD_REQUEST, LocalDateTime.now(), Arrays.toString(ex.getStackTrace()));
    }

    private ErrorResponse handleIllegalArgumentException(Exception ex) {
        return ErrorResponse.of(ex.getMessage(), HttpStatus.BAD_REQUEST, LocalDateTime.now(), Arrays.toString(ex.getStackTrace()));
    }

    private ErrorResponse handleGenericException(Throwable ex) {
        return ErrorResponse.of(ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now(), Arrays.toString(ex.getStackTrace()));
    }

    private ErrorResponse handleR2dbcDataIntegrityViolationException(R2dbcDataIntegrityViolationException ex) {
        String details = "Violación de integridad de datos: " + ex.getMessage();
        return ErrorResponse.of(details, HttpStatus.CONFLICT, LocalDateTime.now(), Arrays.toString(ex.getStackTrace()));
    }

    private ErrorResponse handleR2dbcBadGrammarException(R2dbcBadGrammarException ex) {
        String details = "Error de sintaxis SQL: " + ex.getMessage();
        return ErrorResponse.of(details, HttpStatus.BAD_REQUEST, LocalDateTime.now(), Arrays.toString(ex.getStackTrace()));
    }


    private HttpStatus mapToHttpStatus(VitalitSoftException.Type type) {
        return switch (type) {

            case UNAUTHORIZED,
                 UNAUTHORIZED_ACCESS,
                 INVALID_AUTH,
                 MISSING_AUTH,
                 API_KEY_REQUIRED,
                 INVALID_TOKEN,
                 TOKEN_EXPIRED,
                 TOKEN_EXPIRED_OR_INVALID,
                 TOKEN_NOT_FOUND,
                 REFRESH_INVALID_TOKEN,
                 REFRESH_TOKEN_EXPIRED,
                 TOKEN_ALREADY_USED,
                 COOKIE_NOT_FOUND -> HttpStatus.UNAUTHORIZED;

            case ACCOUNT_LOCKED,
                 OTP_BLOCKED -> HttpStatus.FORBIDDEN;

            case USER_NOT_FOUND,
                 SESSION_ID_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case EMAIL_ALREADY_EXISTS,
                 EMAIL_ALREADY_REGISTERED -> HttpStatus.CONFLICT;

            case INVALID_PASSWORD,
                 PASSWORD_MISMATCH,
                 OTP_INVALID,
                 OTP_EXPIRED,
                 OTP_ALREADY_USED,
                 OTP_MAX_ATTEMPTS,
                 OTP_MAX_RESEND_ATTEMPTS,
                 PENDING_VERIFICATION -> HttpStatus.BAD_REQUEST;

            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
