package co.com.bancolombia.security.mock.shared.exception;

public class CustomException extends RuntimeException {
    private final String code;
    private final int httpStatus;

    public CustomException(String message) {
        this(message, null, 500);
    }

    public CustomException(String message, int httpStatus) {
        this(message, null, httpStatus);
    }

    public CustomException(String message, String code) {
        this(message, code, 500);
    }
    
    public CustomException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
    
    public String getCode(){
        return code;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}
