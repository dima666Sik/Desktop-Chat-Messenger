package ua.desktop.chat.messenger.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemHandling;
import ua.desktop.chat.messenger.domain.ifaces.MessageSystemHandling;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
                            connectionHandler.getUserNameList().add("GLOBAL");
                            addClient(userDTO.getUsername(), this);
                            System.out.println("1." + " " + connectionHandler.getUserNameList().size());
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
                                        System.out.println(messageDTO);

                                        String msgJSON = ParserJSON.convertObjectToString(messageDTO, TypeMessage.MESSAGE_OBJECT);
                                        String userJSON = ParserJSON.convertObjectToString(userDTO, TypeMessage.USER_OBJECT);

                                        connectionHandler.sendMessage(this, receiver, userJSON, msgJSON);

                                        new Thread(() -> {
                                            synchronized (this) {
                                                //2. TODO Send message in db in chat current user
                                                if (connectionHandler.getChatSystemMessaging().isExistChatByUser(receiver, userDTO.getId())) {

                                                    Chat chatORM = connectionHandler.getChatSystemMessaging().getChat(receiver, userDTO.getId());
                                                    Message messageORM = new Message(messageDTO, chatORM);

                                                    connectionHandler.getMessageSystemHandling().createMessageByChat(messageORM);
                                                } else throw new RuntimeException("Message in chat was not added!");
                                            }
                                        }).start();
                                    }
                                    break;
                                }
                            } else {
                                out.println("/M");
                                String msgJSON = ParserJSON.convertObjectToString("[NOBODY IS HERE. YOUR MESSAGES NOT SAVED!]", TypeMessage.STRING_NOTIFICATION);
                                out.println(msgJSON);
                            }
                            break OUTER;
                        case "/EXIT":
                            removeClient(userDTO.getUsername());
                            conn.close();
                            isActive = false;
                            break;
                    }
                }
            } catch (SocketException e) {
                try {
                    removeClient(userDTO.getUsername());
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

    public void sendUserNameList(Set<String> ul) {
        StringBuilder userListResponse = new StringBuilder();
        out.println("/USERS");
        System.out.println("+++++1: " + ul.size());
        User userORM = new User(userDTO);
        ul.forEach(System.out::print);
        for (String userChat : ul) {
            System.out.println("+++++2");
            // 1. TODO Add chats with this names into db if them not exist there
            System.out.println("+++" + userChat);
            System.out.println("+++++2.5");
            userListResponse.append(userChat).append(",");
            new Thread(() -> {
                synchronized (this) {
                    if (!connectionHandler.getChatSystemMessaging().isExistChatByUser(userChat, userDTO.getId())) {
                        if (userChat.equals(TypeChat.GLOBAL.name())) {
                            System.out.println(userChat + " " + userORM.getUsername() + " " + userORM.getId());
                            connectionHandler.getChatSystemMessaging().createChatByUser(TypeChat.GLOBAL.name(), TypeChat.GLOBAL, userORM);
                        } else {
                            if (!userChat.equals(userORM.getUsername())) {
                                System.out.println(userChat + " " + userORM.getUsername() + " " + userORM.getId());
                                connectionHandler.getChatSystemMessaging().createChatByUser(userChat, TypeChat.PRIVATE, userORM);
                            }
                        }
                    }
                }
            }).start();
            System.out.println("+++++4");
        }
        out.println(userListResponse);
    }

    public synchronized void addClient(String username, ClientHandler ch) {
        System.out.println("+++1");
        connectionHandler.getClientHandlers().put(username, ch);
        System.out.println(connectionHandler.getClientHandlers());
        System.out.println("+++1.5 " + connectionHandler.getClientHandlers().size());
        System.out.println("+++2");
        informAllClientsUserNameList();
    }

    public synchronized void removeClient(String username) {
        connectionHandler.getClientHandlers().remove(username);
        connectionHandler.getUserNameList().remove(username);
        informAllClientsUserNameList();
    }

    public void informAllClientsUserNameList() {
        System.out.println("+++3");
        fillChatNameList();
        System.out.println("+++4");

        if (flagGlobalChat) {
            this.sendUserNameList(connectionHandler.getUserNameList());
            flagGlobalChat = false;
        }

        for (String key : connectionHandler.getClientHandlers().keySet()) {
            ClientHandler client = connectionHandler.getClientHandlers().get(key);
            client.sendUserNameList(connectionHandler.getUserNameList());
        }
    }

    private void fillChatNameList() {
        System.out.println("+++3.5");
        System.out.println("2." + " " + connectionHandler.getClientHandlers().size());
        System.out.println("2." + " " + connectionHandler.getUserNameList().size());

        for (String key : connectionHandler.getClientHandlers().keySet()) {
            ClientHandler client = connectionHandler.getClientHandlers().get(key);
            System.out.println("___" + client.getUsername() + "+++" + userDTO.getUsername());
            connectionHandler.getUserNameList().add(client.getUsername());
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
