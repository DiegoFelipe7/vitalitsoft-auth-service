package com.vitalitsoft.infrastructure.entry.points.api.shared.utilities;

public final class ExceptionUtils {

    private ExceptionUtils() {}

    public static String origin(Throwable ex) {
        StackTraceElement element = ex.getStackTrace()[0];
        return String.format(
                "%s.%s:%d",
                element.getClassName(),
                element.getMethodName(),
                element.getLineNumber()
        );
    }

    public static String rootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getClass().getSimpleName();
    }
}
