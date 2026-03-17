package com.vitalitsoft.application.command.otp;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VerifyOtpCommand {
    String sessionId;
    String otp;
    boolean inactiveTwoFactor;
}
