package ua.desktop.chat.messenger.server.service.chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.server.service.ClientHandler;
import ua.desktop.chat.messenger.server.service.ConnectionHandler;

import java.io.IOException;

public class UserInitializer {
    private static final Logger logger = LogManager.getLogger(UserInitializer.class);
    private UserDTO userDTO;
    private final ClientHandler clientHandler;
    private final ConnectionHandler connectionHandler;

    public UserInitializer(ClientHandler clientHandler, ConnectionHandler connectionHandler) {
        this.clientHandler = clientHandler;
        this.connectionHandler = connectionHandler;
    }

    public void initializeUser() throws IOException, OpenSessionException {
        String receivedObjectString;
        if (userDTO == null) {
            while ((receivedObjectString = clientHandler.getSocketInputReader().readLine()) != null) {
                userDTO = (UserDTO) ParserJSON.convertStringToObject(receivedObjectString);
                if (connectionHandler.getClientHandlers().containsKey(userDTO.getUsername())) {
                    logger.info("You cannot chose this username");
                } else if (!userDTO.getUsername().isEmpty()) {
                    logger.info("Username accepted: {}", userDTO.getUsername());
                    clientHandler
                            .getChatManagerProcessGUI()
                            .addClient(userDTO.getUsername(), clientHandler);
                    break;
                } else {
                    logger.info("No Username chosen.");
                }
            }
        }
    }

    public String getUsername() {
        return userDTO.getUsername();
    }
    public UserDTO getUserDTO() {
        return userDTO;
    }
}
