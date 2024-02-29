package ua.desktop.chat.messenger.core.service.exception;

public class ProcessingMessageException extends Exception{
    public ProcessingMessageException() {
    }

    public ProcessingMessageException(String message) {
        super(message);
    }

    public ProcessingMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessingMessageException(Throwable cause) {
        super(cause);
    }

    public ProcessingMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
