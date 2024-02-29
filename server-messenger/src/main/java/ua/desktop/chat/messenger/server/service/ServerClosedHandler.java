package ua.desktop.chat.messenger.server.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.server.service.exception.SocketClosedException;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerClosedHandler {
    private static final Logger logger = LogManager.getLogger(ServerClosedHandler.class);
    private final ConnectionHandler connectionHandler;

    public ServerClosedHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }
    public void closeServerSocket() {
        try {
            if (connectionHandler.getServerSocket() != null) {
                connectionHandler
                        .getServerSocket()
                        .close();
            }
        } catch (IOException e) {
            logger.error("Unable to close. IOException", e);
            throw new SocketClosedException("Unable to close. IOException", e);
        }
    }
}
