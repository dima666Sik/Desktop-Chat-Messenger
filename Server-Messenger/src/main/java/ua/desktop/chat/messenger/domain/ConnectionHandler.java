package ua.desktop.chat.messenger.domain;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemHandling;
import ua.desktop.chat.messenger.domain.ifaces.MessageSystemHandling;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.ui.ServerGUI;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ConnectionHandler implements Runnable {
    private final static Logger logger = LogManager.getLogger(ConnectionHandler.class.getName());
    private Map<String, ClientHandler> clientHandlers = new HashMap<>();
    private final Multimap<String, ChatDTO> userNameAndChatInfo = ArrayListMultimap.create();
    private Boolean isActive = true;
    private Boolean newUser = true;
    private final int portNumber;
    private final ChatSystemHandling chatSystemMessaging;
    private final MessageSystemHandling messageSystemHandling;
    private ServerGUI serverGUI;
    private ServerSocket serverSocket;

    public ConnectionHandler(int portNumber, ChatSystemHandling chatSystemMessaging, MessageSystemHandling messageSystemHandling) {
        this.portNumber = portNumber;
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

    public void sendMessage(ClientHandler sender, String clientRCVR, String userJSON, String msgJSON) {

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
                ClientHandler client = clientHandlers.get(key);
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
                //2. TODO Send message in db in chat current user
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

    public synchronized Map<String, ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public synchronized void setClientHandlers(Map<String, ClientHandler> clients) {
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
