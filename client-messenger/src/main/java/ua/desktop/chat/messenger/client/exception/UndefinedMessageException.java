package ua.desktop.chat.messenger.client.exception;

public class UndefinedMessageException extends RuntimeException{
    public UndefinedMessageException() {
    }

    public UndefinedMessageException(String message) {
        super(message);
    }

    public UndefinedMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public UndefinedMessageException(Throwable cause) {
        super(cause);
    }

    public UndefinedMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
