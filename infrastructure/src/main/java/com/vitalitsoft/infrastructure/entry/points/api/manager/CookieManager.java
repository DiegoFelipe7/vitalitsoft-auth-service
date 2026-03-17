package com.vitalitsoft.infrastructure.entry.points.api.manager;


import com.vitalitsoft.domain.shared.exception.VitalitSoftException;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.time.Duration;


public class CookieManager {

    private CookieManager() {
        throw new IllegalStateException("Utility class");
    }

    public static ResponseCookie createSecureCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(1))
                .build();
    }

    public static ResponseCookie createSecureCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(maxAge)
                .build();
    }

    public static ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build();
    }

    public static Mono<String> getCookieValue(ServerRequest request, String name) {
        return Mono.justOrEmpty(request.cookies().getFirst(name))
                .map(HttpCookie::getValue)
                .switchIfEmpty(Mono.error(new VitalitSoftException(VitalitSoftException.Type.COOKIE_NOT_FOUND)));
    }
}