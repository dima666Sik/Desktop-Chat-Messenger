package ua.desktop.chat.messenger.client.exception;

public class ReadBufferException extends RuntimeException{
    public ReadBufferException() {
    }

    public ReadBufferException(String message) {
        super(message);
    }

    public ReadBufferException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadBufferException(Throwable cause) {
        super(cause);
    }

    public ReadBufferException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
