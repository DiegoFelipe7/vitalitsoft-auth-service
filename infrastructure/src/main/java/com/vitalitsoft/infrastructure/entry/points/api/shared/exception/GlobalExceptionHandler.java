package com.vitalitsoft.infrastructure.entry.points.api.shared.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.vitalitsoft.domain.shared.exception.NexusException;
import com.vitalitsoft.infrastructure.entry.points.api.shared.utilities.ExceptionUtils;
import tools.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
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
        if (ex instanceof NexusException customException) {
            return handleCustomException(customException);
        } else if (ex instanceof WebExchangeBindException validationException) {
            return handleValidationException(validationException);
        } else if (ex instanceof InvalidFormatException invalidFormatException) {
            return handleInvalidFormatException(invalidFormatException);
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

    private ErrorResponse handleCustomException(NexusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getHttpStatus());
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
}
