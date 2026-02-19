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

@Component
public class AuthHeaderFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        String userId = request.headers().firstHeader(Headers.USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            return Mono.error(new NexusException("userId header is required", HttpStatus.BAD_REQUEST));
        }
        return next.handle(request);
    }
}
