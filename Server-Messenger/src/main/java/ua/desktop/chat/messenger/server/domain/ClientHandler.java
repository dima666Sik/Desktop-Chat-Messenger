package ua.desktop.chat.messenger.server.domain;

import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.server.domain.ifaces.Observer;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

public class ClientHandler implements Runnable, Observer {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class.getName());
    private final ConnectionHandler connectionHandler;
    private final Socket conn;
    private volatile boolean isActive = true;
    private UserDTO userDTO;
    private final BufferedReader in;
    private final PrintStream out;

    public ClientHandler(ConnectionHandler cHandler, Socket conn) {
        try {
            this.connectionHandler = cHandler;
            this.conn = conn;
            //get socket writing and reading streams
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            out = new PrintStream(conn.getOutputStream(), true);
        } catch (Exception e) {
            logger.error("Unable to init streams in clientHandler", e);
            throw new RuntimeException("Unable to init streams in clientHandler", e);
        }
    }

    public void run() {
        String receivedObjectString, userInput;
        while (isActive) {
            try {
                if (userDTO == null) {
                    while ((receivedObjectString = in.readLine()) != null) {
                        userDTO = (UserDTO) ParserJSON.convertStringToObject(receivedObjectString);
                        if (connectionHandler.getClientHandlers().containsKey(userDTO.getUsername())) {
                            logger.info("You cannot chose this username");
                        } else if (!userDTO.getUsername().isEmpty()) {
                            logger.info("Username accepted: ".concat(userDTO.getUsername()));
                            connectionHandler.addClient(userDTO.getUsername(), this);
                            break;
                        } else {
                            logger.info("No Username chosen.");
                        }
                    }
                }

                OUTER:
                while ((userInput = in.readLine()) != null) {
                    switch (userInput) {
                        case "/M":
                            MessageDTO messageDTO;
                            if (connectionHandler.getClientHandlers().size() > 1) {
                                String receiver;
                                if ((receiver = in.readLine()) != null) {
                                    logger.info("SENDER / " + receiver);
                                    if ((receivedObjectString = in.readLine()) != null) {
                                        messageDTO = (MessageDTO) ParserJSON.convertStringToObject(receivedObjectString);
                                        logger.info("userName:  ".concat(userDTO.getUsername()).concat(", message: ").concat(messageDTO.getMessage()));

                                        String msgJSON = ParserJSON.convertObjectToString(messageDTO, TypeMessage.MESSAGE_OBJECT);
                                        String userJSON = ParserJSON.convertObjectToString(userDTO, TypeMessage.USER_OBJECT);

                                        connectionHandler.sendMessage(this, receiver, userJSON, msgJSON);
                                        connectionHandler.sendMessageInDB(receiver, userDTO, messageDTO);
                                    }
                                    break;
                                }
                            } else {
                                out.println("/M");
                                String msgJSON = ParserJSON.convertObjectToString("[NOBODY IS HERE. YOUR MESSAGES NOT SAVED!]", TypeMessage.STRING_NOTIFICATION);
                                out.println(msgJSON);

                                logger.info("NOBODY IS HERE. YOUR MESSAGES NOT SAVED! this message for once user in chat: ".concat(userDTO.getUsername()));
                                connectionHandler.getServerGUI().updateChat("NOBODY IS HERE. YOUR MESSAGES NOT SAVED! this message for once user in chat: ".concat(userDTO.getUsername()));
                            }
                            break OUTER;
                        case "/UPDATE GROUP INTO LIST":
                            connectionHandler.informAllClientsUserNameList();
                            break OUTER;
                        case "/EXIT":
                            connectionHandler.removeClient(userDTO.getUsername(), new ChatDTO(TypeChat.PRIVATE, userDTO.getId(), userDTO));

                            logger.info("Client was removed from the list chat! Name current client is: ".concat(userDTO.getUsername()));
                            connectionHandler.getServerGUI().updateChat("Client was removed from the list chat! Name current client is: ".concat(userDTO.getUsername()));

                            conn.close();
                            isActive = false;
                            break;
                    }
                }
            } catch (SocketException e) {
                try {
                    connectionHandler.removeClient(userDTO.getUsername(), new ChatDTO(TypeChat.PRIVATE, userDTO.getId(), userDTO));
                    conn.close();
                    isActive = false;
                    logger.warn("Socket was closed. U successful exit from client!");
                } catch (IOException e1) {
                    logger.error("SocketException. Problem with closed Socket.", e1);
                    throw new RuntimeException("SocketException. Problem with closed Socket.", e1);
                }
            } catch (Exception e) {
                logger.error("Unable to exit from clientHandler, problem with close Socket.", e);
                throw new RuntimeException("Unable to exit from clientHandler, problem with close Socket.", e);
            }
        }
    }

    @Override
    public void sendUserNameList(Multimap<String, ChatDTO> ul) {
        StringBuilder userListResponse = new StringBuilder();
        out.println("/USERS");

        for (Map.Entry<String, ChatDTO> entry : ul.entries()) {
            String userChat = entry.getKey();
            ChatDTO chatDTO = entry.getValue();

            // Add chats with current names into db if them not exist there
            userListResponse.append(userChat)
                    .append(":").append(chatDTO.getTypeChat())
                    .append(":").append(chatDTO.getUser().getUsername()).append(",");
            System.out.println("..." + userListResponse);

            connectionHandler.createChatsIfNotExist(userChat, chatDTO, userDTO);
        }
        out.println(userListResponse);
    }

    @Override
    public void sendMessage(String msg) {
        out.println("/M");
        out.println(msg);
    }

    public String getUsername() {
        return userDTO.getUsername();
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

}
