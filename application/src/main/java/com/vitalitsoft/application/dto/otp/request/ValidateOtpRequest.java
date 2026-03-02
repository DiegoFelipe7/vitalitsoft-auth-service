package com.vitalitsoft.application.dto.otp.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateOtpRequest {
    @NotBlank(message = "El session id es obligatoria.")
    private String sessionId;
    @NotBlank(message = "La otp es obligatoria.")
    private String otp;
    private boolean inactiveTwoFactor = false;
}
