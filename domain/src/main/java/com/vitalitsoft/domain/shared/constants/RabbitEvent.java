package com.vitalitsoft.domain.shared.constants;

public class RabbitEvent {
    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_PASSWORD_RESET = "USER_PASSWORD_RESET";
    public static final String GENERATE_OTP = "GENERATE_OTP";

    private RabbitEvent() {
        throw new IllegalStateException("Constants class");
    }
}
