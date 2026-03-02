package com.vitalitsoft.application.dto.passwordReset.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResetPasswordResponse {
    private String email;
    private String message;
}
