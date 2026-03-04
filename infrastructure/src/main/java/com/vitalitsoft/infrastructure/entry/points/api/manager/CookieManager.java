package com.vitalitsoft.infrastructure.entry.points.api.manager;


import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.exception.NexusException;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class CookieManager {

    private CookieManager() {
        throw new IllegalStateException("Manager class");
    }


    public static ResponseCookie createCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();
    }




    public static ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }


    public static Mono<String> getCookieValue(ServerRequest request, String name) {
        return Mono.justOrEmpty(request.cookies().getFirst(name))
                .map(HttpCookie::getValue)
                .switchIfEmpty(Mono.error(new NexusException(NexusException.Type.UNAUTHORIZED, HttpStatus.UNAUTHORIZED)));
    }

    public static ResponseCookie refreshCookie(String name, String value) {
        return createCookie(name, value);
    }


    public static Mono<String> getOptionalCookie(ServerRequest request, String name) {
        HttpCookie cookie = request.cookies().getFirst(name);
        return Mono.justOrEmpty(cookie).map(HttpCookie::getValue);
    }

}