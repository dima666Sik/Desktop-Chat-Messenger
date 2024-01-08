package ua.desktop.chat.messenger.server.service.chat;

import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.server.service.exception.SocketClosedException;
import ua.desktop.chat.messenger.server.service.ClientHandler;
import ua.desktop.chat.messenger.server.service.ConnectionHandler;

import java.io.IOException;
import java.util.Map;

public class ChatExitHandler {
    private static final Logger logger = LogManager.getLogger(ChatExitHandler.class);
    private final ClientHandler clientHandler;
    private final ConnectionHandler connectionHandler;

    public ChatExitHandler(ClientHandler clientHandler, ConnectionHandler connectionHandler) {
        this.clientHandler = clientHandler;
        this.connectionHandler = connectionHandler;
    }

    void processExitCommand() throws IOException {
        removeClient(clientHandler.getInitializeUser().getUserDTO().getUsername(),
                new ChatDTO(TypeChat.PRIVATE, clientHandler.getInitializeUser().getUserDTO().getId(), clientHandler.getInitializeUser().getUserDTO()));

        logger.info("Client was removed from the list chat! Name current client is: {}", clientHandler.getInitializeUser().getUsername());
        connectionHandler.getServerGUI().updateChat("Client was removed from the list chat! Name current client is: " + clientHandler.getInitializeUser().getUsername());

        clientHandler.getSocket().close();
        clientHandler.setActive(false);
    }

    public void exitUser() {
        try {
            removeClient(clientHandler.getInitializeUser().getUsername(), new ChatDTO(TypeChat.PRIVATE,
                    clientHandler
                            .getInitializeUser()
                            .getUserDTO()
                            .getId(),
                    clientHandler
                    .getInitializeUser()
                    .getUserDTO()));
            clientHandler.getSocket().close();
            clientHandler.setActive(false);
            logger.warn("Socket was closed. U successful exit from client!");
        } catch (IOException e) {
            logger.error("SocketException. Problem with closed Socket.", e);
            throw new SocketClosedException("SocketException. Problem with closed Socket.", e);
        }
    }

    public synchronized void removeClient(String username, ChatDTO chatDTO) {
        Map<String, ClientHandler> clientHandlers =  connectionHandler.getClientHandlers();
        clientHandlers.remove(username);

        Multimap<String, ChatDTO> userNameAndChatInfo = connectionHandler.getUserNameAndChatInfo();
        userNameAndChatInfo.remove(username, chatDTO);

        clientHandler.getChatManager().informAllClientsUserNameList();

    }
}
