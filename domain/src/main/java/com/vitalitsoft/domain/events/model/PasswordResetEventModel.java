package com.vitalitsoft.domain.events.model;

import lombok.*;

@Builder(toBuilder = true)
public record PasswordResetEventModel(
        String token,
        String email
) {
}
