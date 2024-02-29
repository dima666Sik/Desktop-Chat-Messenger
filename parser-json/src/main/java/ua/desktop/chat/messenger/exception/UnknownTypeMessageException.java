package ua.desktop.chat.messenger.exception;

public class UnknownTypeMessageException extends RuntimeException{
    public UnknownTypeMessageException() {
    }

    public UnknownTypeMessageException(String message) {
        super(message);
    }

    public UnknownTypeMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownTypeMessageException(Throwable cause) {
        super(cause);
    }

    public UnknownTypeMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
