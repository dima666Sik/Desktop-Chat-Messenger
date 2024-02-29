package ua.desktop.chat.messenger.server.service.exception;

public class SocketClosedException extends RuntimeException{
    public SocketClosedException() {
    }

    public SocketClosedException(String message) {
        super(message);
    }

    public SocketClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketClosedException(Throwable cause) {
        super(cause);
    }

    public SocketClosedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
