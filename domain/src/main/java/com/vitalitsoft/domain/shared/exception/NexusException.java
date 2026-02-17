package com.vitalitsoft.domain.shared.exception;

public class NexusException extends CustomException {
    
    public NexusException(String message, int httpStatus) {
        super(message,httpStatus);
    }

    public NexusException(String message, String code) {
        super(message, code);
    }
    
    public NexusException(String message, String code, int httpStatus) {
        super(message, code, httpStatus);
    }
}
