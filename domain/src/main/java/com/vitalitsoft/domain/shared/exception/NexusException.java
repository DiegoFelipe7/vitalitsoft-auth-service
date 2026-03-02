package com.vitalitsoft.domain.shared.exception;

import java.io.Serial;

public class NexusException extends CustomException {
    @Serial
    private static final long serialVersionUID = 1L;
    public enum Type {
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
        UNAUTHORIZED_ACCESS,
        TOKEN_ALREADY_USED,
        PENDING_VERIFICATION,
        API_KEY_REQUIRED,
        MISSING_AUTH,
        INVALID_AUTH,
        INTERNAL_ERROR
    }

    public NexusException(String message, int httpStatus) {
        super(message, httpStatus);
    }

    public NexusException(String message, String code) {
        super(message, code);
    }

    public NexusException(Type message, int httpStatus) {
        super(message.name(), httpStatus);
    }

    public NexusException(String message, String code, int httpStatus) {
        super(message, code, httpStatus);
    }
}
