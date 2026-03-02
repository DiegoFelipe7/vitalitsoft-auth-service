package com.vitalitsoft.application.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshSessionResponse {
    private String token;
    private String refreshToken;
    private Boolean isTwoFactorAuthRequired;
}
