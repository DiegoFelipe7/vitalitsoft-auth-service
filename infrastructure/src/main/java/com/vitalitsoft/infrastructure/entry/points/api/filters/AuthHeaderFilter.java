package com.vitalitsoft.infrastructure.entry.points.api.filters;

import com.vitalitsoft.domain.shared.constants.Headers;
import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

@Component
public class AuthHeaderFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {


    @Override
    @NonNull
    public Mono<ServerResponse> filter(@NonNull ServerRequest request, @NonNull HandlerFunction<ServerResponse> next) {
        var userId = request.headers().firstHeader(Headers.USER_ID_HEADER);
        if (userId.isBlank()) {
            return Mono.error(new NexusException("userId header is required", HttpStatus.BAD_REQUEST));
        }
        return next.handle(request);
    }
}
