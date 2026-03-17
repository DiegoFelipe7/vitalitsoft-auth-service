package com.vitalitsoft.application.command.passwordReset;

import com.vitalitsoft.domain.shared.enums.TokenType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConfirmPasswordResetCommand {
    String password;
    String token;
    TokenType tokenType;
}
