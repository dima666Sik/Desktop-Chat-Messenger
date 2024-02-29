package ua.desktop.chat.messenger.server.service.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.constant.ChatConstant;
import ua.desktop.chat.messenger.constant.MessageConstant;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.server.service.ClientHandler;
import ua.desktop.chat.messenger.server.service.ConnectionHandler;
import ua.desktop.chat.messenger.server.service.exception.AddMessageException;
import ua.desktop.chat.messenger.server.service.exception.UndefinedChatException;

import java.io.IOException;
import java.util.Optional;

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

    public void processUserMessage() throws IOException {
        String receivedObjectString;
        MessageDTO messageDTO;
        if (connectionHandler
                .getClientHandlers()
                .size() > 1) {
            String receiver;
            if ((receiver = clientHandler.getSocketInputReader()
                                         .readLine()) != null) {
                logger.info("SENDER / {}", receiver);
                if ((receivedObjectString = clientHandler.getSocketInputReader()
                                                         .readLine()) != null) {
                    messageDTO = (MessageDTO) ParserJSON.convertStringToObject(receivedObjectString);
                    logger.info("userName:  {}, message: {}",
                            clientHandler.getInitializeUser()
                                         .getUsername(),
                            messageDTO.getMessage());

                    String msgJSON = ParserJSON.convertObjectToString(messageDTO, TypeMessage.MESSAGE_OBJECT);
                    String userJSON = ParserJSON.convertObjectToString(clientHandler
                            .getInitializeUser()
                            .getUserDTO(), TypeMessage.USER_OBJECT);

                    sendMessage(clientHandler, receiver, userJSON, msgJSON);
                    sendMessageInDB(receiver, clientHandler.getInitializeUser()
                                                           .getUserDTO(), messageDTO);
                }
            }
        } else {
            clientHandler.getSocketOutputWriter()
                         .println(ChatConstant.MESSAGE_COMMAND);
            String msgJSON = ParserJSON.convertObjectToString(MessageConstant.NOBODY_IS_HERE_MESSAGE, TypeMessage.STRING_NOTIFICATION);
            clientHandler.getSocketOutputWriter()
                         .println(msgJSON);

            logger.info("{} this message for once user in chat: {}",
                    MessageConstant.NOBODY_IS_HERE_MESSAGE, clientHandler.getInitializeUser()
                                                                         .getUsername());
            connectionHandler
                    .getServerHandlerGUI()
                    .updateChat(MessageConstant.NOBODY_IS_HERE_MESSAGE
                            + " this message for once user in chat: "
                            + clientHandler
                            .getInitializeUser()
                            .getUsername());
        }
    }

    public void sendMessage(String msg) {
        clientHandler.getSocketOutputWriter()
                     .println(ChatConstant.MESSAGE_COMMAND);
        clientHandler.getSocketOutputWriter()
                     .println(msg);
    }

    public void sendMessageInDB(String receiver, UserDTO userDTO, MessageDTO message) {
        new Thread(() -> {
            synchronized (this) {
                try {
                    // Send message in db in chat current user
                    if (connectionHandler.getChatSystemMessaging()
                                         .isExistChatByUser(receiver, userDTO.getId())) {

                        Optional<ChatDTO> chatDTO = connectionHandler.getChatSystemMessaging()
                                                                     .readChat(receiver, userDTO.getId());
                        if (chatDTO.isEmpty()) throw new UndefinedChatException("Chat was not found!");
                        MessageDTO messageDTO = new MessageDTO(message.getMessage(), message.getLocalDateTime(), chatDTO.get());

                        connectionHandler.getMessageSystemHandling()
                                         .createMessageByChat(messageDTO);
                        logger.info("Message is successful adding into db! Message owner is: {}", userDTO.getUsername());
                        connectionHandler.getServerHandlerGUI()
                                         .updateChat("Message is successful adding into db! Message owner is: " + userDTO.getUsername());

                    } else throw new AddMessageException("Message in chat was not added!");
                } catch (OpenSessionException e) {
                    logger.error("Opening Session was not successful!", e);
                }
            }
        }).start();
    }
}
