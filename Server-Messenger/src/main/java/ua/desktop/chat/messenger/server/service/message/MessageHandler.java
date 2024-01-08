package ua.desktop.chat.messenger.server.service.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.server.service.ClientHandler;
import ua.desktop.chat.messenger.server.service.ConnectionHandler;

import java.io.IOException;

public class MessageHandler {
    private static final Logger logger = LogManager.getLogger(MessageHandler.class);
    private final ClientHandler clientHandler;
    private final ConnectionHandler connectionHandler;

    public MessageHandler(ClientHandler clientHandler, ConnectionHandler connectionHandler) {
        this.clientHandler = clientHandler;
        this.connectionHandler = connectionHandler;
    }

    public void sendMessage(ClientHandler sender, String clientRCVR, String userJSON, String msgJSON) {

        UserDTO user = (UserDTO) ParserJSON.convertStringToObject(userJSON);
        MessageDTO message = (MessageDTO) ParserJSON.convertStringToObject(msgJSON);

        if (!clientHandler
                .getChatManager()
                .checkCountCompanion(user, message, sender)) return;

        if (connectionHandler
                .getMessageManagerProcessGUI()
                .sendNonPrivateMessage(user, message)) return;

        connectionHandler
                .getMessageManagerProcessGUI()
                .sendPrivateMessage(user, message, sender, clientRCVR);
    }

    void processUserMessage() throws IOException {
        String receivedObjectString;
        MessageDTO messageDTO;
        if (connectionHandler
                .getClientHandlers()
                .size() > 1) {
            String receiver;
            if ((receiver = clientHandler.getSocketInputReader().readLine()) != null) {
                logger.info("SENDER / {}", receiver);
                if ((receivedObjectString = clientHandler.getSocketInputReader().readLine()) != null) {
                    messageDTO = (MessageDTO) ParserJSON.convertStringToObject(receivedObjectString);
                    logger.info("userName:  {}, message: {}",
                            clientHandler.getInitializeUser().getUsername(),
                            messageDTO.getMessage());

                    String msgJSON = ParserJSON.convertObjectToString(messageDTO, TypeMessage.MESSAGE_OBJECT);
                    String userJSON = ParserJSON.convertObjectToString(clientHandler
                            .getInitializeUser()
                            .getUserDTO(), TypeMessage.USER_OBJECT);

                    sendMessage(clientHandler, receiver, userJSON, msgJSON);
                    connectionHandler.sendMessageInDB(receiver, clientHandler.getInitializeUser().getUserDTO(), messageDTO);
                }
            }
        } else {
            clientHandler.getSocketOutputWriter().println("/M");
            String msgJSON = ParserJSON.convertObjectToString("[NOBODY IS HERE. YOUR MESSAGES NOT SAVED!]", TypeMessage.STRING_NOTIFICATION);
            clientHandler.getSocketOutputWriter().println(msgJSON);

            logger.info("NOBODY IS HERE. YOUR MESSAGES NOT SAVED! this message for once user in chat: {}", clientHandler.getInitializeUser().getUsername());
            connectionHandler
                    .getServerGUI()
                    .updateChat("NOBODY IS HERE. YOUR MESSAGES NOT SAVED! this message for once user in chat: "
                            + clientHandler
                            .getInitializeUser()
                            .getUsername());
        }
    }

    public void sendMessage(String msg) {
        clientHandler.getSocketOutputWriter().println("/M");
        clientHandler.getSocketOutputWriter().println(msg);
    }
}
