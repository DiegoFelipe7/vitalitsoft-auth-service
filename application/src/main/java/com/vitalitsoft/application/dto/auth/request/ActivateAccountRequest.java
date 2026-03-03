package com.vitalitsoft.application.dto.auth.request;

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
    @NotBlank(message = "El token es obligatorio.")
    private String token;
}
