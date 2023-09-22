package ua.desktop.chat.messenger.domain;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.prop.PropertiesFile;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemHandling;
import ua.desktop.chat.messenger.domain.ifaces.MessageSystemHandling;
import ua.desktop.chat.messenger.domain.ifaces.Observer;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.ui.ServerGUI;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ConnectionHandler implements Runnable {
    private final static Logger logger = LogManager.getLogger(ConnectionHandler.class.getName());
    private Map<String, Observer> clientHandlers = new HashMap<>();
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

                logger.info("InetAddress : " + serverSocket.getInetAddress());
                serverGUI.updateChat("InetAddress : " + serverSocket.getInetAddress());

                while (!serverSocket.isClosed()) {
                    if (newUser) {
                        newUser = false;
                    } else {
                        Socket conn = serverSocket.accept();

                        logger.info("Connection received from " + conn.getInetAddress().getHostName() + " : " + conn.getPort());
                        serverGUI.updateChat("Connection received from " + conn.getInetAddress().getHostName() + " : " + conn.getPort());

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

    public void sendMessage(Observer sender, String clientRCVR, String userJSON, String msgJSON) {

        UserDTO user = (UserDTO) ParserJSON.convertStringToObject(userJSON);
        MessageDTO message = (MessageDTO) ParserJSON.convertStringToObject(msgJSON);

        if (clientHandlers.size() <= 1) {
            message.setMessage("[MESSAGE COULD NOT BE SEND!]");
            sender.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

            logger.info("MESSAGE COULD NOT BE SEND! this message for once user in chat: ".concat(user.getUsername()));
            serverGUI.updateChat("MESSAGE COULD NOT BE SEND! this message for once user in chat: ".concat(user.getUsername()));
            return;
        }

        if (message.getChat().getTypeChat() == TypeChat.GLOBAL || message.getChat().getTypeChat() == TypeChat.GROUP) {
            message.setMessage("[".concat(message.getChat().getTypeChat().name()).concat("] ").concat(user.getUsername()).concat(": ").concat(message.getMessage()));
            for (String key : clientHandlers.keySet()) {
                Observer client = clientHandlers.get(key);
                client.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

                logger.info("Send message (GLOBAL|GROUP) is successful! Message owner is: ".concat(user.getUsername()));
                serverGUI.updateChat("Send message (GLOBAL|GROUP) is successful! Message owner is: ".concat(user.getUsername()));
            }
        } else {
            try {
                if (clientHandlers.containsKey(clientRCVR)) {
                    message.setMessage("[".concat(message.getChat().getTypeChat().name()).concat("] ").concat(user.getUsername()).concat(": ").concat(message.getMessage()));
                    clientHandlers.get(clientRCVR).sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

                    logger.info("Send message (PRIVATE) is successful! Message owner is: ".concat(user.getUsername()).concat(". Companion is: ").concat(clientRCVR));
                    serverGUI.updateChat("Send message (PRIVATE) is successful! Message owner is: ".concat(user.getUsername()).concat(". Companion is: ").concat(clientRCVR));
                } else {
                    message.setMessage("[MESSAGE COULD NOT BE SEND!]");
                    sender.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

                    logger.info("MESSAGE COULD NOT BE SEND! this message for once user in chat: ".concat(user.getUsername()));
                    serverGUI.updateChat("MESSAGE COULD NOT BE SEND! this message for once user in chat: ".concat(user.getUsername()));
                }
            } catch (Exception e) {
                logger.error("Unable to send message on clientHandler", e);
                throw new RuntimeException("Unable to send message on clientHandler", e);
            }
        }
    }

    public void sendMessageInDB(String receiver, UserDTO userDTO, MessageDTO message) {
        new Thread(() -> {
            synchronized (this) {
                // Send message in db in chat current user
                if (chatSystemMessaging.isExistChatByUser(receiver, userDTO.getId())) {

                    Optional<ChatDTO> chatDTO = chatSystemMessaging.readChat(receiver, userDTO.getId());
                    if (chatDTO.isEmpty()) throw new RuntimeException("Chat was not found!");
                    MessageDTO messageDTO = new MessageDTO(message.getMessage(), message.getLocalDateTime(), chatDTO.get());

                    if (messageSystemHandling.createMessageByChat(messageDTO)) {
                        logger.info("Message is successful adding into db! Message owner is: ".concat(userDTO.getUsername()));
                        serverGUI.updateChat("Message is successful adding into db! Message owner is: ".concat(userDTO.getUsername()));
                    }
                } else throw new RuntimeException("Message in chat was not added!");
            }
        }).start();
    }

    public synchronized void addClient(String username, Observer ch) {
        clientHandlers.put(username, ch);
        informAllClientsUserNameList();

        logger.info("Client was added into list chat! Name current client is: ".concat(username));
        serverGUI.updateChat("Client was added into list chat! Name current client is: ".concat(username));
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

        for (String key : clientHandlers.keySet()) {
            Observer client = clientHandlers.get(key);
            client.sendUserNameList(userNameAndChatInfo);
        }
    }

    private void fillChatNameList() {

        for (String key : clientHandlers.keySet()) {
            Observer client = clientHandlers.get(key);
            userNameAndChatInfo.put("GLOBAL", new ChatDTO(TypeChat.GLOBAL, null, client.getUserDTO()));
            userNameAndChatInfo.put(client.getUsername(), new ChatDTO(TypeChat.PRIVATE, client.getUserDTO().getId(), client.getUserDTO()));
        }
    }

    private void fillChatNameListGroup() {
        for (String key : clientHandlers.keySet()) {
            Observer client = clientHandlers.get(key);
            // read chats with type group!
            Optional<List<ChatDTO>> chatList = chatSystemMessaging.readChatsByType(TypeChat.GROUP, client.getUserDTO().getId());
            if (chatList.isEmpty()) throw new RuntimeException("Chats was not found!");
            for (ChatDTO chat : chatList.get()) {
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
                    } else if (chatDTO.getTypeChat() == TypeChat.PRIVATE) {
                        if (!userChat.equals(currentUserDTO.getUsername())) {
                            chatSystemMessaging.createChatByUser(userChat, TypeChat.PRIVATE, currentUserDTO, chatDTO.getUserCompanionId());
                        }
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
            throw new RuntimeException("Unable to close. IOException", e);
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

    public synchronized Map<String, Observer> getClientHandlers() {
        return clientHandlers;
    }

    public synchronized void setClientHandlers(Map<String, Observer> clients) {
        this.clientHandlers = clients;
    }

    public synchronized ChatSystemHandling getChatSystemMessaging() {
        return chatSystemMessaging;
    }

    public synchronized MessageSystemHandling getMessageSystemHandling() {
        return messageSystemHandling;
    }

    public synchronized Multimap<String, ChatDTO> getUserNameAndChatInfo() {
        return userNameAndChatInfo;
    }
}
