package com.vitalitsoft.application.dto.passwordReset;

import com.vitalitsoft.domain.shared.enums.TokenType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResetRequest {
    @NotBlank(message = "La token es obligatorio.")
    private String token;
    @NotNull(message = "El tipo de token es obligatorio.")
    private TokenType tokenType;
}

