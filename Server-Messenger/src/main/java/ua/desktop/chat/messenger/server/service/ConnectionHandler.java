package ua.desktop.chat.messenger.server.service.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.core.service.MessageSystemHandling;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
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
    private Map<String, ClientHandler> clientHandlers = new HashMap<>();
    private final Multimap<String, ChatDTO> userNameAndChatInfo = ArrayListMultimap.create();
    private volatile Boolean isActive = true;
    private Boolean newUser = true;
    private final ChatSystemHandling chatSystemMessaging;
    private final MessageSystemHandling messageSystemHandling;
    private ServerGUI serverGUI;
    private ServerSocket serverSocket;
    private MessageHandlerGUI messageHandlerGUI;

    public ConnectionHandler(ChatSystemHandling chatSystemMessaging, MessageSystemHandling messageSystemHandling) {
        this.chatSystemMessaging = chatSystemMessaging;
        this.messageSystemHandling = messageSystemHandling;
    }

    public void run() {
        serverGUI = new ServerGUI();
        messageHandlerGUI = new MessageHandlerGUI(this);
        serverGUI.startGUI();
        serverGUI.setServer(this);

        while (isActive) {

            try {
                InetAddress addr = InetAddress.getLocalHost();
                int portNumber = ServerConfiguration.getServerPort();
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

    public synchronized Multimap<String, ChatDTO> getUserNameAndChatInfo() {
        return userNameAndChatInfo;
    }
    public synchronized MessageHandlerGUI getMessageManagerProcessGUI() {
        return messageHandlerGUI;
    }
    public synchronized ChatSystemHandling getChatSystemMessaging() {
        return chatSystemMessaging;
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
}
