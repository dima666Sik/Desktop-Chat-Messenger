package ua.desktop.chat.messenger.server.service.exception;

public class AddMessageException extends RuntimeException{
    public AddMessageException() {
    }

    public AddMessageException(String message) {
        super(message);
    }

    public AddMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public AddMessageException(Throwable cause) {
        super(cause);
    }

    public AddMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
