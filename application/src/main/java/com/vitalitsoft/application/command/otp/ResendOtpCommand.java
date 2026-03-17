package com.vitalitsoft.application.command.otp;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResendOtpCommand {
    String sessionId;
}
