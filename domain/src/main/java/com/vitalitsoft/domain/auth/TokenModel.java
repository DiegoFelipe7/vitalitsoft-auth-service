package com.vitalitsoft.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.NoArgsConstructor;

@Getter
@With
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TokenModel{
    private String accessToken;
    private String refreshToken;
    private Boolean isTwoFactorAuthRequired;
    private String sessionId;
}