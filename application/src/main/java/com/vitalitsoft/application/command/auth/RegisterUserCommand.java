package com.vitalitsoft.application.command.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterUserCommand {
    String firstName;
    String lastName;
    String email;
    String password;
    String phoneNumber;
    Boolean termsAccepted;
}
