package com.vitalitsoft.application.command.passwordReset;

import com.vitalitsoft.domain.shared.enums.TokenType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ValidateTokenResetCommand {
    String token;
    TokenType tokenType;
}
