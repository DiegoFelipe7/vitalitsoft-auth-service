package com.vitalitsoft.application.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {
    private UUID userId;
    private String email;
    private String message;
}
