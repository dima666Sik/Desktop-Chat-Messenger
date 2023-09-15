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

public class ClientHandler implements Runnable {
    private final static Logger logger = LogManager.getLogger(ConnectionHandler.class.getName());
    private final ConnectionHandler connectionHandler;
    private final Socket conn;
    private Boolean isActive = true;
    private UserDTO userDTO;
    private final BufferedReader in;
    private final PrintStream out;
    private boolean flagGlobalChat = true;

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

                                        new Thread(() -> {
                                            synchronized (this) {
                                                //2. TODO Send message in db in chat current user
                                                if (connectionHandler.getChatSystemMessaging().isExistChatByUser(receiver, userDTO.getId())) {

                                                    Chat chatORM = connectionHandler.getChatSystemMessaging().readChat(receiver, userDTO.getId());
                                                    Message messageORM = new Message(messageDTO, chatORM);

                                                    connectionHandler.getMessageSystemHandling().createMessageByChat(messageORM);
                                                } else throw new RuntimeException("Message in chat was not added!");
                                            }
                                        }).start();
                                    }
                                    break;
                                }
                            }
                            else {
                                out.println("/M");
                                String msgJSON = ParserJSON.convertObjectToString("[NOBODY IS HERE. YOUR MESSAGES NOT SAVED!]", TypeMessage.STRING_NOTIFICATION);
                                out.println(msgJSON);
                            }
                            break OUTER;
                        case "/EXIT":
                            removeClient(userDTO.getUsername(), new ChatDTO(TypeChat.PRIVATE, userDTO.getId(), userDTO));
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
        User userORM = new User(userDTO);

        for (Map.Entry<String, ChatDTO> entry : ul.entries()) {
            String userChat = entry.getKey();
            ChatDTO chatDTO = entry.getValue();

            // 1. TODO Add chats with this names into db if them not exist there
            userListResponse.append(userChat)
                    .append(":").append(chatDTO.getTypeChat())
                    .append(":").append(chatDTO.getUser().getUsername()).append(",");
            System.out.println("..."+userListResponse);
            new Thread(() -> {
                synchronized (this) {
                    if (!connectionHandler.getChatSystemMessaging().isExistChatByUser(userChat, userDTO.getId())) {
                        if (chatDTO.getTypeChat() == TypeChat.GLOBAL) {
                            connectionHandler.getChatSystemMessaging().createChatByUser(TypeChat.GLOBAL.name(), TypeChat.GLOBAL, userORM, chatDTO.getUserCompanionId());
                        } else if (chatDTO.getTypeChat() == TypeChat.PRIVATE) {
                            if (!userChat.equals(userORM.getUsername())) {
                                connectionHandler.getChatSystemMessaging().createChatByUser(userChat, TypeChat.PRIVATE, userORM, chatDTO.getUserCompanionId());
                            }
                        }
                    }
                }
            }).start();
        }
        out.println(userListResponse);
    }

    public synchronized void addClient(String username, ClientHandler ch) {
        connectionHandler.getClientHandlers().put(username, ch);
        informAllClientsUserNameList();
    }

    public synchronized void removeClient(String username, ChatDTO chatDTO) {
        connectionHandler.getClientHandlers().remove(username);
//        connectionHandler.getUserNameAndChatInfo().remove(username, chatDTO);
        informAllClientsUserNameList();
    }

    public void informAllClientsUserNameList() {
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
        System.out.println("++");
        for (String key : connectionHandler.getClientHandlers().keySet()) {
            ClientHandler client = connectionHandler.getClientHandlers().get(key);
            // TODO read chats with type group!

            List<Chat> chatList = connectionHandler.getChatSystemMessaging().readChatsByType(TypeChat.GROUP, client.getUserDTO().getId());
            for (Chat chat : chatList) {
                connectionHandler.getUserNameAndChatInfo().put(chat.getNameChat(), new ChatDTO(TypeChat.GROUP, null, client.getUserDTO()));
                System.out.println("++" + connectionHandler.getUserNameAndChatInfo().get(chat.getNameChat()));
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
