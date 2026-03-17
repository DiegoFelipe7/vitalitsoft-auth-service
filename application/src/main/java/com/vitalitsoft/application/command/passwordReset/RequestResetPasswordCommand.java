package com.vitalitsoft.application.command.passwordReset;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RequestResetPasswordCommand {
    String email;
}
