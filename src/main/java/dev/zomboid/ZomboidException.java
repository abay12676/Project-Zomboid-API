package dev.zomboid;

public class ZomboidException extends Exception {

    public ZomboidException() {
    }

    public ZomboidException(String message) {
        super(message);
    }

    public ZomboidException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZomboidException(Throwable cause) {
        super(cause);
    }

    public ZomboidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
