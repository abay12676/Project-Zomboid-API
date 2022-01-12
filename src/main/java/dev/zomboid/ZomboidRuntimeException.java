package dev.zomboid;

public class ZomboidRuntimeException extends RuntimeException {

    public ZomboidRuntimeException() {
    }

    public ZomboidRuntimeException(String message) {
        super(message);
    }

    public ZomboidRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZomboidRuntimeException(Throwable cause) {
        super(cause);
    }

    public ZomboidRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
