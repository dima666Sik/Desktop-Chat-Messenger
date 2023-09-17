package ua.desktop.chat.messenger.domain;

import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private final static Logger logger = LogManager.getLogger(ConnectionHandler.class.getName());
    private final ConnectionHandler connectionHandler;
    private final Socket conn;
    private Boolean isActive = true;
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
                            addClient(userDTO.getUsername(), this);
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
                        case "/EXIT":
                            removeClient(userDTO.getUsername(), new ChatDTO(TypeChat.PRIVATE, userDTO.getId(), userDTO));

                            logger.info("Client was removed from the list chat! Name current client is: ".concat(userDTO.getUsername()));
                            connectionHandler.getServerGUI().updateChat("Client was removed from the list chat! Name current client is: ".concat(userDTO.getUsername()));

                            conn.close();
                            isActive = false;
                            break;
                    }
                }
            } catch (SocketException e) {
                try {
                    removeClient(userDTO.getUsername(), new ChatDTO(TypeChat.PRIVATE, userDTO.getId(), userDTO));
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

    public void sendUserNameList(Multimap<String, ChatDTO> ul) {
        StringBuilder userListResponse = new StringBuilder();
        out.println("/USERS");

        for (Map.Entry<String, ChatDTO> entry : ul.entries()) {
            String userChat = entry.getKey();
            ChatDTO chatDTO = entry.getValue();

            // 1. TODO Add chats with this names into db if them not exist there
            userListResponse.append(userChat)
                    .append(":").append(chatDTO.getTypeChat())
                    .append(":").append(chatDTO.getUser().getUsername()).append(",");
            System.out.println("..." + userListResponse);

            createChatsIfNotExist(userChat, chatDTO);
        }
        out.println(userListResponse);
    }

    private void createChatsIfNotExist(String userChat, ChatDTO chatDTO) {
        new Thread(() -> {
            synchronized (this) {
                if (!connectionHandler.getChatSystemMessaging().isExistChatByUser(userChat, userDTO.getId())) {
                    if (chatDTO.getTypeChat() == TypeChat.GLOBAL) {
                        connectionHandler.getChatSystemMessaging().createChatByUser(TypeChat.GLOBAL.name(), TypeChat.GLOBAL, userDTO, chatDTO.getUserCompanionId());
                    } else if (chatDTO.getTypeChat() == TypeChat.PRIVATE) {
                        if (!userChat.equals(userDTO.getUsername())) {
                            connectionHandler.getChatSystemMessaging().createChatByUser(userChat, TypeChat.PRIVATE, userDTO, chatDTO.getUserCompanionId());
                        }
                    }
                }
            }
        }).start();
    }

    private synchronized void addClient(String username, ClientHandler ch) {
        connectionHandler.getClientHandlers().put(username, ch);
        informAllClientsUserNameList();

        logger.info("Client was added into list chat! Name current client is: ".concat(username));
        connectionHandler.getServerGUI().updateChat("Client was added into list chat! Name current client is: ".concat(username));
    }

    private synchronized void removeClient(String username, ChatDTO chatDTO) {
        connectionHandler.getClientHandlers().remove(username);
        connectionHandler.getUserNameAndChatInfo().remove(username, chatDTO);
        informAllClientsUserNameList();

    }

    private void informAllClientsUserNameList() {
        connectionHandler.getUserNameAndChatInfo().clear();
        fillChatNameList();
        fillChatNameListGroup();

        for (String key : connectionHandler.getClientHandlers().keySet()) {
            ClientHandler client = connectionHandler.getClientHandlers().get(key);
            client.sendUserNameList(connectionHandler.getUserNameAndChatInfo());
        }
    }

    private void fillChatNameList() {

        for (String key : connectionHandler.getClientHandlers().keySet()) {
            ClientHandler client = connectionHandler.getClientHandlers().get(key);
            connectionHandler.getUserNameAndChatInfo().put("GLOBAL", new ChatDTO(TypeChat.GLOBAL, null, client.getUserDTO()));
            connectionHandler.getUserNameAndChatInfo().put(client.getUsername(), new ChatDTO(TypeChat.PRIVATE, client.getUserDTO().getId(), client.getUserDTO()));
        }
    }

    private void fillChatNameListGroup() {
        for (String key : connectionHandler.getClientHandlers().keySet()) {
            ClientHandler client = connectionHandler.getClientHandlers().get(key);
            // TODO read chats with type group!

            Optional<List<ChatDTO>> chatList = connectionHandler.getChatSystemMessaging().readChatsByType(TypeChat.GROUP, client.getUserDTO().getId());
            if (chatList.isEmpty()) throw new RuntimeException("Chats was not found!");
            for (ChatDTO chat : chatList.get()) {
                connectionHandler.getUserNameAndChatInfo().put(chat.getNameChat(), new ChatDTO(chat.getTypeChat(), null, chat.getUser()));
            }
        }
    }

    public void sendMessage(String msg) {
        out.println("/M");
        out.println(msg);
    }

    public void terminate() {
        isActive = false;
    }

    public String getUsername() {
        return userDTO.getUsername();
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

}
