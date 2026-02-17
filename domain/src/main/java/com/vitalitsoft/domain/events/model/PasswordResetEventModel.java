package com.vitalitsoft.domain.events.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PasswordResetEventModel {
    private String token;
    private String email;
}
