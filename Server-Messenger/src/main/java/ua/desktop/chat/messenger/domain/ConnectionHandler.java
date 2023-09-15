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
import ua.desktop.chat.messenger.parser.ParserJSON;

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

    public ConnectionHandler(int portNumber, ChatSystemHandling chatSystemMessaging, MessageSystemHandling messageSystemHandling) {
        this.portNumber = portNumber;
        this.chatSystemMessaging = chatSystemMessaging;
        this.messageSystemHandling = messageSystemHandling;
    }

    public void run() {
        while (isActive) {
            ServerSocket s = null;
            try {
                InetAddress addr = InetAddress.getLocalHost();

                s = new ServerSocket(portNumber, 10, addr);
                s.setReuseAddress(true);
                logger.info("InetAddress : " + s.getInetAddress());

                while (!s.isClosed()) {
                    if (newUser) {
                        newUser = false;
                    } else {
                        Socket conn = s.accept();

                        logger.info("Connection received from " + conn.getInetAddress().getHostName() + " : " + conn.getPort());

                        Runnable runnableCH = new ClientHandler(this, conn);
                        Thread thread = new Thread(runnableCH);
                        thread.start();
                    }
                }
            } catch (IOException e) {
                logger.error(e);
                throw new RuntimeException(e);
            }

            try {
                s.close();
            } catch (IOException e) {
                logger.error("Unable to close. IOException", e);
                throw new RuntimeException("Unable to close. IOException", e);
            }
        }
    }

    public void sendMessage(ClientHandler sender, String clientRCVR, String userJSON, String msgJSON) {

        UserDTO user = (UserDTO) ParserJSON.convertStringToObject(userJSON);
        MessageDTO message = (MessageDTO) ParserJSON.convertStringToObject(msgJSON);

        if (clientHandlers.size() <= 1) {
            message.setMessage("[MESSAGE COULD NOT BE SEND!]");
            sender.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));
            return;
        }

        if (message.getChat().getTypeChat() == TypeChat.GLOBAL || message.getChat().getTypeChat() == TypeChat.GROUP) {
            message.setMessage("[".concat(message.getChat().getTypeChat().name()).concat("] ").concat(user.getUsername()).concat(": ").concat(message.getMessage()));
            for (String key : clientHandlers.keySet()) {
                ClientHandler client = clientHandlers.get(key);
                client.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));
            }
        } else {
            try {
                System.out.println("---" + user.getUsername() + " " + clientRCVR + " " + clientHandlers.size());
                if (clientHandlers.containsKey(clientRCVR)) {
                    message.setMessage("[".concat(message.getChat().getTypeChat().name()).concat("] ").concat(user.getUsername()).concat(": ").concat(message.getMessage()));
                    clientHandlers.get(clientRCVR).sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));
                } else {
                    message.setMessage("[MESSAGE COULD NOT BE SEND!]");
                    sender.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));
                }
            } catch (Exception e) {
                logger.error("Unable to send message on clientHandler", e);
                throw new RuntimeException("Unable to send message on clientHandler", e);
            }
        }
    }

    public void terminate() {
        isActive = false;
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
