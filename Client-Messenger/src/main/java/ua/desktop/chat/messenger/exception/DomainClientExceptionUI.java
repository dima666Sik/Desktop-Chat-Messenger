package ua.desktop.chat.messenger.exception;

import javax.swing.*;

public class DomainClientExceptionUI extends JOptionPane {
    public DomainClientExceptionUI() {
    }

    public DomainClientExceptionUI(Object message) {
        super(message);
    }

    public DomainClientExceptionUI(Object message, int messageType) {
        super(message, messageType);
    }

    public DomainClientExceptionUI(Object message, int messageType, int optionType) {
        super(message, messageType, optionType);
    }
}