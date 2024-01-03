package ua.desktop.chat.messenger.core.domain.exceptions;

public class DomainException extends Exception {
    public DomainException() {
    }

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomainException(String myMessage, String systemMessage) {
    }
}
