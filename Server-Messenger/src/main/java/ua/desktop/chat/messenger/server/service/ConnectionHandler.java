package ua.desktop.chat.messenger.server.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.core.service.MessageSystemHandling;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.server.service.message.MessageHandlerGUI;

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
    private ServerSocket serverSocket;
    private MessageHandlerGUI messageHandlerGUI;
    private ServerClosedHandler serverClosedHandler;
    private ServerHandlerGUI serverHandlerGUI;

    public ConnectionHandler(ChatSystemHandling chatSystemMessaging, MessageSystemHandling messageSystemHandling) {
        this.chatSystemMessaging = chatSystemMessaging;
        this.messageSystemHandling = messageSystemHandling;
    }

    public void run() {
        serverHandlerGUI = new ServerHandlerGUI();
        messageHandlerGUI = new MessageHandlerGUI(this);
        serverClosedHandler = new ServerClosedHandler(serverSocket);
        serverHandlerGUI.setServer(this);

        while (isActive) {

            try {
                InetAddress addr = InetAddress.getLocalHost();
                int portNumber = ServerPortConfiguration.getServerPort();
                serverSocket = new ServerSocket(portNumber, 10, addr);
                serverSocket.setReuseAddress(true);

                logger.info("InetAddress : {}", serverSocket.getInetAddress());
                serverHandlerGUI.updateChat("InetAddress : " + serverSocket.getInetAddress());

                while (!serverSocket.isClosed()) {
                    if (newUser) {
                        newUser = false;
                    } else {
                        Socket conn = serverSocket.accept();

                        logger.info("Connection received from {} : {}",
                                conn.getInetAddress().getHostName(), conn.getPort());
                        serverHandlerGUI.updateChat("Connection received from "
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

    public synchronized ServerHandlerGUI getServerHandlerGUI() {
        return serverHandlerGUI;
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
