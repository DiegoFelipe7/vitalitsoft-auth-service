package com.vitalitsoft.application.dto.otp.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendOtpRequest {
    @NotNull(message = "Session ID es requerido")
    private String sessionId;
}
