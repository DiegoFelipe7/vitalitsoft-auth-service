package com.vitalitsoft.application.dto.auth.request;

import com.vitalitsoft.domain.shared.enums.TokenType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateAccountRequest {
    @NotBlank(message = "El correo electrónico es obligatorio.")
    private String token;
    @NotBlank(message = "El correo electrónico es obligatorio.")
    private TokenType tokenType;
}
