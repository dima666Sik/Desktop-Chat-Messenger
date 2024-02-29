package ua.desktop.chat.messenger.exception;

public class EncryptionException extends Exception {
    public EncryptionException() {
    }

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptionException(String myMessage, String systemMessage) {
    }
}
