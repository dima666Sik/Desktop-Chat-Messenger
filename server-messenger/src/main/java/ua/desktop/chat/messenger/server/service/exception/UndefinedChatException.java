package ua.desktop.chat.messenger.server.service.exception;

public class UndefinedChatException extends RuntimeException{
    public UndefinedChatException() {
    }

    public UndefinedChatException(String message) {
        super(message);
    }

    public UndefinedChatException(String message, Throwable cause) {
        super(message, cause);
    }

    public UndefinedChatException(Throwable cause) {
        super(cause);
    }

    public UndefinedChatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
