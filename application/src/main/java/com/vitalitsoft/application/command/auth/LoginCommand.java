package com.vitalitsoft.application.command.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginCommand {
    String email;
    String password;
}
