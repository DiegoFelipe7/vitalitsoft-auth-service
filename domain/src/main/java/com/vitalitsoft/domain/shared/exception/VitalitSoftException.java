package com.vitalitsoft.domain.shared.exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class VitalitSoftException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Type type;


    public enum Type {
        COOKIE_NOT_FOUND,
        USER_NOT_FOUND,
        INVALID_PASSWORD,
        REFRESH_INVALID_TOKEN,
        EMAIL_ALREADY_EXISTS,
        INVALID_TOKEN,
        REFRESH_TOKEN_EXPIRED,
        TOKEN_EXPIRED,
        TOKEN_EXPIRED_OR_INVALID,
        TOKEN_NOT_FOUND,
        EMAIL_ALREADY_REGISTERED,
        SESSION_ID_NOT_FOUND,
        OTP_MAX_RESEND_ATTEMPTS,
        OTP_ALREADY_USED,
        OTP_EXPIRED,
        OTP_INVALID,
        OTP_BLOCKED,
        OTP_MAX_ATTEMPTS,
        PASSWORD_MISMATCH,
        ACCOUNT_LOCKED,
        UNAUTHORIZED,
        UNAUTHORIZED_ACCESS,
        TOKEN_ALREADY_USED,
        PENDING_VERIFICATION,
        API_KEY_REQUIRED,
        MISSING_AUTH,
        INVALID_AUTH,
        INTERNAL_ERROR
    }

    public VitalitSoftException(Type type) {
        super(type.name());
        this.type = type;
    }
    
    public VitalitSoftException(String message) {
        super(message);
        this.type = Type.INTERNAL_ERROR;
    }

}
