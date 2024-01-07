package ua.desktop.chat.messenger.core.service.exception;

public class ProcessingChatException extends RuntimeException{
    public ProcessingChatException() {
    }

    public ProcessingChatException(String message) {
        super(message);
    }

    public ProcessingChatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessingChatException(Throwable cause) {
        super(cause);
    }

    public ProcessingChatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
