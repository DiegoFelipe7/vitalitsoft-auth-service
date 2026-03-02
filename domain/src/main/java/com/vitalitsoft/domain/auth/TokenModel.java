package co.com.bancolombia.security.mock.auth;

import lombok.*;

@Getter
@With
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TokenModel{
    private String token;
    private String refreshToken;
    private Boolean isTwoFactorAuthRequired;
}