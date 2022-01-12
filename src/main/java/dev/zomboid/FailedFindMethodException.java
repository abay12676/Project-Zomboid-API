package dev.zomboid;

public class FailedFindMethodException  extends ZomboidRuntimeException {

    public FailedFindMethodException() {
    }

    public FailedFindMethodException(String message) {
        super(message);
    }

    public FailedFindMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedFindMethodException(Throwable cause) {
        super(cause);
    }

    public FailedFindMethodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
