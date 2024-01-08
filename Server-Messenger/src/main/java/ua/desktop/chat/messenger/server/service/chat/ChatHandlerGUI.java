package ua.desktop.chat.messenger.server.service.chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.server.service.ClientHandler;
import ua.desktop.chat.messenger.server.service.ConnectionHandler;

import java.util.Map;

public class ChatHandlerGUI {
    private static final Logger logger = LogManager.getLogger(ChatHandlerGUI.class);
    private final ConnectionHandler connectionHandler;
    private final ClientHandler clientHandler;

    public ChatHandlerGUI(ClientHandler clientHandler, ConnectionHandler connectionHandler) {
        this.clientHandler = clientHandler;
        this.connectionHandler = connectionHandler;
    }

    public synchronized void addClient(String username, ClientHandler ch) {
        Map<String, ClientHandler> clientHandlers =  connectionHandler.getClientHandlers();
        clientHandlers.put(username, ch);

        clientHandler.getChatManager().informAllClientsUserNameList();

        logger.info("Client was added into list chat! Name current client is: {}", username);
        connectionHandler
                .getServerGUI()
                .updateChat("Client was added into list chat! Name current client is: " + username);
    }

}
