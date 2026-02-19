package com.vitalitsoft.application.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpResponse {
    private boolean success;
    private String message;
}
