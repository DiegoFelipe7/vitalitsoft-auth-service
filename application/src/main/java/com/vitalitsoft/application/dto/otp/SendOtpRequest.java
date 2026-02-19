package com.vitalitsoft.application.dto.otp;

import lombok.Data;

@Data
public class SendOtpRequest {
    private String destination; // email o teléfono
    private String channel; // "email" o "sms"
}
