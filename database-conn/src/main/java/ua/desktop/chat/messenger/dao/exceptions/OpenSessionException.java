package ua.desktop.chat.messenger.dao.exceptions;

public class OpenSessionException extends Exception {
    public OpenSessionException() {
    }

    public OpenSessionException(String message) {
        super(message);
    }

    public OpenSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenSessionException(String myMessage, String systemMessage) {
    }
}
