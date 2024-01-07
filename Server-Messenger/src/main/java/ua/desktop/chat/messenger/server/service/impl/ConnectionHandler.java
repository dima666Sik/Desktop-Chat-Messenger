package ua.desktop.chat.messenger.server.service.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.prop.PropertiesFile;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.core.service.MessageSystemHandling;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.server.service.ClientObserver;
import ua.desktop.chat.messenger.server.service.exception.AddMessageException;
import ua.desktop.chat.messenger.server.service.exception.SocketClosedException;
import ua.desktop.chat.messenger.server.service.exception.UndefinedChatException;
import ua.desktop.chat.messenger.server.ui.ServerGUI;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ConnectionHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(ConnectionHandler.class.getName());
    private Map<String, ClientObserver> clientHandlers = new HashMap<>();
    private final Multimap<String, ChatDTO> userNameAndChatInfo = ArrayListMultimap.create();
    private volatile Boolean isActive = true;
    private Boolean newUser = true;
    private final ChatSystemHandling chatSystemMessaging;
    private final MessageSystemHandling messageSystemHandling;
    private ServerGUI serverGUI;
    private ServerSocket serverSocket;
    private static final String NAME_PROP_FILE = "server_connection.properties";
    private static final String PROP_VALUE_SERVER_PORT = "server.connection.port";

    public ConnectionHandler(ChatSystemHandling chatSystemMessaging, MessageSystemHandling messageSystemHandling) {
        this.chatSystemMessaging = chatSystemMessaging;
        this.messageSystemHandling = messageSystemHandling;
    }

    public void run() {
        serverGUI = new ServerGUI();
        serverGUI.startGUI();
        serverGUI.setServer(this);

        while (isActive) {

            try {
                InetAddress addr = InetAddress.getLocalHost();
                int portNumber = Integer.parseInt(PropertiesFile.getProp(NAME_PROP_FILE).getProperty(PROP_VALUE_SERVER_PORT));
                serverSocket = new ServerSocket(portNumber, 10, addr);
                serverSocket.setReuseAddress(true);

                logger.info("InetAddress : {}", serverSocket.getInetAddress());
                serverGUI.updateChat("InetAddress : " + serverSocket.getInetAddress());

                while (!serverSocket.isClosed()) {
                    if (newUser) {
                        newUser = false;
                    } else {
                        Socket conn = serverSocket.accept();

                        logger.info("Connection received from {} : {}",
                                conn.getInetAddress().getHostName(), conn.getPort());
                        serverGUI.updateChat("Connection received from "
                                + conn.getInetAddress().getHostName()
                                + " : " + conn.getPort());

                        Runnable runnableCH = new ClientHandler(this, conn);
                        Thread thread = new Thread(runnableCH);
                        thread.start();
                    }
                }
            } catch (IOException e) {
                logger.warn("You interrupted accept call! Close server!");
            }
        }
    }

    public void sendMessage(ClientObserver sender, String clientRCVR, String userJSON, String msgJSON) {

        UserDTO user = (UserDTO) ParserJSON.convertStringToObject(userJSON);
        MessageDTO message = (MessageDTO) ParserJSON.convertStringToObject(msgJSON);

        if (!checkCountCompanion(user, message, sender)) return;

        if (sendNonPrivateMessage(user, message)) return;

        sendPrivateMessage(user, message, sender, clientRCVR);
    }

    private void sendPrivateMessage(UserDTO user, MessageDTO message, ClientObserver sender, String clientRCVR) {
        if (clientHandlers.containsKey(clientRCVR)) {
            message.setMessage("["
                    + message.getChat().getTypeChat().name()
                    + "] " + user.getUsername()
                    + ": " + message.getMessage());
            clientHandlers.get(clientRCVR).sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

            logger.info("Send message (PRIVATE) is successful! Message owner is: {}. Companion is: {}", user.getUsername(), clientRCVR);
            serverGUI.updateChat("Send message (PRIVATE) is successful! Message owner is: " + user.getUsername() + ". Companion is: " + clientRCVR);
        } else {
            message.setMessage("[MESSAGE COULD NOT BE SEND!]");
            sender.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

            logger.info("MESSAGE COULD NOT BE SEND! this message for once user in chat: {}", user.getUsername());
            serverGUI.updateChat("MESSAGE COULD NOT BE SEND! this message for once user in chat: " + user.getUsername());
        }
    }

    private boolean sendNonPrivateMessage(UserDTO user, MessageDTO message) {
        if (message.getChat().getTypeChat() == TypeChat.GLOBAL || message.getChat().getTypeChat() == TypeChat.GROUP) {
            message.setMessage("["
                    + message.getChat().getTypeChat().name()
                    + "] " + user.getUsername()
                    + ": " + message.getMessage());
            for (Map.Entry<String, ClientObserver> key : clientHandlers.entrySet()) {
                ClientObserver client = key.getValue();
                client.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

                logger.info("Send message (GLOBAL|GROUP) is successful! Message owner is: {}", user.getUsername());
                serverGUI.updateChat("Send message (GLOBAL|GROUP) is successful! Message owner is: " + user.getUsername());
            }
            return true;
        }
        return false;
    }

    private boolean checkCountCompanion(UserDTO user, MessageDTO message, ClientObserver sender) {
        if (clientHandlers.size() <= 1) {
            message.setMessage("[MESSAGE COULD NOT BE SEND!]");
            sender.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

            logger.info("MESSAGE COULD NOT BE SEND! this message for once user in chat: {}", user.getUsername());
            serverGUI.updateChat("MESSAGE COULD NOT BE SEND! this message for once user in chat: " + user.getUsername());
            return false;
        }
        return true;
    }

    public void sendMessageInDB(String receiver, UserDTO userDTO, MessageDTO message) {
        new Thread(() -> {
            synchronized (this) {
                // Send message in db in chat current user
                if (chatSystemMessaging.isExistChatByUser(receiver, userDTO.getId())) {

                    Optional<ChatDTO> chatDTO = chatSystemMessaging.readChat(receiver, userDTO.getId());
                    if (chatDTO.isEmpty()) throw new UndefinedChatException("Chat was not found!");
                    MessageDTO messageDTO = new MessageDTO(message.getMessage(), message.getLocalDateTime(), chatDTO.get());

                    messageSystemHandling.createMessageByChat(messageDTO);
                    logger.info("Message is successful adding into db! Message owner is: {}", userDTO.getUsername());
                    serverGUI.updateChat("Message is successful adding into db! Message owner is: " + userDTO.getUsername());

                } else throw new AddMessageException("Message in chat was not added!");
            }
        }).start();
    }

    public synchronized void addClient(String username, ClientObserver ch) {
        clientHandlers.put(username, ch);
        informAllClientsUserNameList();

        logger.info("Client was added into list chat! Name current client is: {}", username);
        serverGUI.updateChat("Client was added into list chat! Name current client is: " + username);
    }

    public synchronized void removeClient(String username, ChatDTO chatDTO) {
        clientHandlers.remove(username);
        userNameAndChatInfo.remove(username, chatDTO);
        informAllClientsUserNameList();

    }

    public void informAllClientsUserNameList() {
        userNameAndChatInfo.clear();
        fillChatNameList();
        fillChatNameListGroup();

        for (Map.Entry<String, ClientObserver> key : clientHandlers.entrySet()) {
            ClientObserver client = key.getValue();
            client.sendUserNameList(userNameAndChatInfo);
        }
    }

    private void fillChatNameList() {
        for (Map.Entry<String, ClientObserver> key : clientHandlers.entrySet()) {
            ClientObserver client = key.getValue();
            userNameAndChatInfo.put("GLOBAL", new ChatDTO(TypeChat.GLOBAL, null, client.getUserDTO()));
            userNameAndChatInfo.put(client.getUsername(), new ChatDTO(TypeChat.PRIVATE, client.getUserDTO().getId(), client.getUserDTO()));
        }
    }

    private void fillChatNameListGroup() {
        for (Map.Entry<String, ClientObserver> key : clientHandlers.entrySet()) {
            ClientObserver client = key.getValue();
            // read chats with a type group!
            List<ChatDTO> chatList = chatSystemMessaging.readChatsByType(TypeChat.GROUP, client.getUserDTO().getId());
            if (chatList.isEmpty()) throw new UndefinedChatException("Chats was not found!");
            for (ChatDTO chat : chatList) {
                userNameAndChatInfo.put(chat.getNameChat(), new ChatDTO(chat.getTypeChat(), null, chat.getUser()));
            }
        }
    }

    public void createChatsIfNotExist(String userChat, ChatDTO chatDTO, UserDTO currentUserDTO) {
        new Thread(() -> {
            synchronized (this) {
                if (!chatSystemMessaging.isExistChatByUser(userChat, currentUserDTO.getId())) {
                    if (chatDTO.getTypeChat() == TypeChat.GLOBAL) {
                        chatSystemMessaging.createChatByUser(TypeChat.GLOBAL.name(), TypeChat.GLOBAL, currentUserDTO, chatDTO.getUserCompanionId());
                    } else if (chatDTO.getTypeChat() == TypeChat.PRIVATE
                            && (!userChat.equals(currentUserDTO.getUsername()))) {
                        chatSystemMessaging.createChatByUser(userChat, TypeChat.PRIVATE, currentUserDTO, chatDTO.getUserCompanionId());
                    }
                }
            }
        }).start();
    }

    public synchronized void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Unable to close. IOException", e);
            throw new SocketClosedException("Unable to close. IOException", e);
        }
    }

    public synchronized ServerGUI getServerGUI() {
        return serverGUI;
    }

    public synchronized Boolean getActive() {
        return isActive;
    }

    public synchronized void setActive(Boolean active) {
        isActive = active;
    }

    public synchronized Map<String, ClientObserver> getClientHandlers() {
        return clientHandlers;
    }
}
