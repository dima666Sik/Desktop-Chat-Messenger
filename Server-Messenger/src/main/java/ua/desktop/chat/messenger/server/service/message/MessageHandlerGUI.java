package ua.desktop.chat.messenger.server.service.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.server.service.ClientHandler;
import ua.desktop.chat.messenger.server.service.ConnectionHandler;

import java.util.Map;

public class MessageHandlerGUI {
    private static final Logger logger = LogManager.getLogger(MessageHandlerGUI.class);
    private final ConnectionHandler connectionHandler;

    public MessageHandlerGUI(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    void sendPrivateMessage(UserDTO user, MessageDTO message, ClientHandler sender, String clientRCVR) {
        if (connectionHandler.getClientHandlers().containsKey(clientRCVR)) {
            message.setMessage("["
                    + message.getChat().getTypeChat().name()
                    + "] " + user.getUsername()
                    + ": " + message.getMessage());
            connectionHandler
                    .getClientHandlers()
                    .get(clientRCVR)
                    .getMessageManager()
                    .sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

            logger.info("Send message (PRIVATE) is successful! Message owner is: {}. Companion is: {}", user.getUsername(), clientRCVR);
            connectionHandler
                    .getServerGUI()
                    .updateChat("Send message (PRIVATE) is successful! Message owner is: " + user.getUsername() + ". Companion is: " + clientRCVR);
        } else {
            message.setMessage("[MESSAGE COULD NOT BE SEND!]");
            sender.getMessageManager().sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

            logger.info("MESSAGE COULD NOT BE SEND! this message for once user in chat: {}", user.getUsername());
            connectionHandler
                    .getServerGUI()
                    .updateChat("MESSAGE COULD NOT BE SEND! this message for once user in chat: " + user.getUsername());
        }
    }

    boolean sendNonPrivateMessage(UserDTO user, MessageDTO message) {
        if (message.getChat().getTypeChat() == TypeChat.GLOBAL || message.getChat().getTypeChat() == TypeChat.GROUP) {
            message.setMessage("["
                    + message.getChat().getTypeChat().name()
                    + "] " + user.getUsername()
                    + ": " + message.getMessage());
            for (Map.Entry<String, ClientHandler> key : connectionHandler.getClientHandlers().entrySet()) {
                ClientHandler client = key.getValue();
                client
                        .getMessageManager()
                        .sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

                logger.info("Send message (GLOBAL|GROUP) is successful! Message owner is: {}", user.getUsername());
                connectionHandler
                        .getServerGUI()
                        .updateChat("Send message (GLOBAL|GROUP) is successful! Message owner is: " + user.getUsername());
            }
            return true;
        }
        return false;
    }

}
