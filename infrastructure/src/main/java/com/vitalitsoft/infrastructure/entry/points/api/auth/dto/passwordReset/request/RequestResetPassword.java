package com.vitalitsoft.infrastructure.entry.points.api.auth.dto.passwordReset.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResetPassword {
    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "Debe ingresar un correo electrónico válido.")
    private String email;

}
