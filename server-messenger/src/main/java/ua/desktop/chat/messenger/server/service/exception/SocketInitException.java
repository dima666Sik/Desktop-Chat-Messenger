package ua.desktop.chat.messenger.server.service.exception;

public class SocketInitException extends RuntimeException{
    public SocketInitException() {
    }

    public SocketInitException(String message) {
        super(message);
    }

    public SocketInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketInitException(Throwable cause) {
        super(cause);
    }

    public SocketInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
