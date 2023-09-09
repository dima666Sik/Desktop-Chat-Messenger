package ua.desktop.chat.messenger.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemHandling;
import ua.desktop.chat.messenger.domain.ifaces.MessageSystemHandling;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class CommunicationHandler implements Runnable {
    private final static Logger logger = LogManager.getLogger(CommunicationHandler.class.getName());
    private Boolean isActive = true;
    private Socket s = new Socket();
    private PrintWriter s_out;
    private BufferedReader s_in;
    private Client client;
    private boolean isConnected = false;
    private final ChatSystemHandling chatSystemMessaging;
    private final MessageSystemHandling messageSystemHandling;

    public CommunicationHandler(ChatSystemHandling chatSystemMessaging, MessageSystemHandling messageSystemHandling) {
        this.chatSystemMessaging = chatSystemMessaging;
        this.messageSystemHandling = messageSystemHandling;
    }

    public void run() {
        try {

            Client client = new Client(this);
            Thread thread = new Thread(client);
            thread.start();

            while (isActive) {
                if (getIsConnected()) {
                    String response;
                    while (s_in != null && (response = s_in.readLine()) != null) {
                        logger.info("response: " + response);
                        if (response.equals("/USERS")) {
                            String uList;
                            if ((uList = s_in.readLine()) != null) {
                                Set<String> userList = setUserList(uList);
                                client.updateUserListChatGUI(userList);
                                break;
                            }
                        } else if (response.equals("/M")) {
                            String messageResponse;
                            if ((messageResponse = s_in.readLine()) != null) {
                                Object parsedObject = ParserJSON.convertStringToObject(messageResponse);
                                if (parsedObject instanceof MessageDTO) {
                                    MessageDTO message = (MessageDTO) parsedObject;
                                    client.updateMessageChatGUI(message);
                                } else if (parsedObject instanceof String) {
                                    String textMessage = (String) parsedObject;
                                    client.updateMessageChatGUI(textMessage);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception. Problem with read from buffer.", e);
            throw new RuntimeException("Exception. Problem with read from buffer.", e);
        }
    }

    public Set<String> setUserList(String rspUserList) {
        return new HashSet<>(Arrays.asList(rspUserList.split(",")));
    }

    public synchronized boolean getIsConnected() {
        return isConnected;
    }

    public synchronized void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public synchronized void setActive(Boolean active) {
        isActive = active;
    }

    public synchronized Socket getS() {
        return s;
    }

    public synchronized void setS(Socket s) {
        this.s = s;
    }

    public synchronized PrintWriter getS_out() {
        return s_out;
    }

    public synchronized void setS_out(PrintWriter s_out) {
        this.s_out = s_out;
    }

    public synchronized BufferedReader getS_in() {
        return s_in;
    }

    public synchronized void setS_in(BufferedReader s_in) {
        this.s_in = s_in;
    }

    public synchronized ChatSystemHandling getChatSystemMessaging() {
        return chatSystemMessaging;
    }

    public synchronized MessageSystemHandling getMessageSystemHandling() {
        return messageSystemHandling;
    }
}
