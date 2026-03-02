package com.vitalitsoft.application.dto.passwordReset.request;

import com.vitalitsoft.domain.shared.enums.TokenType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPasswordResetRequest {
    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre 8 y 20 caracteres.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "La contraseña debe incluir al menos una mayúscula, una minúscula, un número y un carácter especial."
    )
    private String password;
    @NotBlank(message = "La token es obligatorio.")
    private String token;
    @NotNull(message = "El tipo de token es obligatorio.")
    private TokenType tokenType;
}
