package ua.desktop.chat.messenger.server.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.server.ui.ServerGUI;


public class ServerHandlerGUI {
    private static final Logger logger = LogManager.getLogger(ServerHandlerGUI.class.getName());

    private final ServerGUI serverGUI;

    public ServerHandlerGUI() {
        this.serverGUI = new ServerGUI();
        this.serverGUI.startGUI();
    }

    public void setServer(ConnectionHandler connectionHandler) {
        serverGUI.setServer(connectionHandler);
    }

    public void updateChat(String message) {
        logger.info(message);
        serverGUI.updateChat(message);
    }
}
