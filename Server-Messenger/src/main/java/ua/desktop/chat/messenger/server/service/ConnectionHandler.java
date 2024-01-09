package ua.desktop.chat.messenger.server.service;

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
import ua.desktop.chat.messenger.server.service.message.MessageHandlerGUI;
import ua.desktop.chat.messenger.server.ui.ServerGUI;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ConnectionHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(ConnectionHandler.class);
    private final Map<String, ClientHandler> clientHandlers = new HashMap<>();
    private final Multimap<String, ChatDTO> userNameAndChatInfo = ArrayListMultimap.create();
    private volatile Boolean isActive = true;
    private Boolean newUser = true;
    private final ChatSystemHandling chatSystemMessaging;
    private final MessageSystemHandling messageSystemHandling;
    private ServerGUI serverGUI;
    private ServerSocket serverSocket;
    private MessageHandlerGUI messageHandlerGUI;
    private ServerClosedHandler serverClosedHandler;

    public ConnectionHandler(ChatSystemHandling chatSystemMessaging, MessageSystemHandling messageSystemHandling) {
        this.chatSystemMessaging = chatSystemMessaging;
        this.messageSystemHandling = messageSystemHandling;
    }

    public void run() {
        serverGUI = new ServerGUI();
        messageHandlerGUI = new MessageHandlerGUI(this);
        serverClosedHandler = new ServerClosedHandler(serverSocket);
        serverGUI.startGUI();
        serverGUI.setServer(this);

        while (isActive) {

            try {
                InetAddress addr = InetAddress.getLocalHost();
                int portNumber = ServerPortConfiguration.getServerPort();
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

    public synchronized ServerClosedHandler getServerClosedHandler() {
        return serverClosedHandler;
    }

    public synchronized MessageSystemHandling getMessageSystemHandling() {
        return messageSystemHandling;
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
