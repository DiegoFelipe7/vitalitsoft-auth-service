package com.vitalitsoft.application.dto.otp;

import lombok.Data;

@Data
public class ValidateOtpRequest {
    private String destination;
    private String otp;
    private String channel;
}
