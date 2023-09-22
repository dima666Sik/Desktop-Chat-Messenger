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
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class CommunicationHandler implements Runnable {
    private final static Logger logger = LogManager.getLogger(CommunicationHandler.class.getName());
    private volatile boolean isActive = true;
    private Socket s;
    private PrintWriter s_out;
    private BufferedReader s_in;
    private volatile boolean isConnected = false;
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
                                Multimap<String, ChatDTO> userList = setUserList(uList);
                                client.updateUserListChatGUI(userList);
                                break;
                            }
                        } else if (response.equals("/M")) {
                            String messageResponse;
                            if ((messageResponse = s_in.readLine()) != null) {
                                Object parsedObject = ParserJSON.convertStringToObject(messageResponse);
                                System.out.println("++++" + parsedObject);
                                if (parsedObject instanceof MessageDTO) {
                                    MessageDTO message = (MessageDTO) parsedObject;
                                    client.updateMessageChatGUI(message);
                                    System.out.println("---00"+message);
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

    public Multimap<String, ChatDTO> setUserList(String rspUserList) {
        Set<String> strChatAndType = new HashSet<>(Arrays.asList(rspUserList.split(",")));
        Multimap<String, ChatDTO> mapTypeChatMap = ArrayListMultimap.create();
        strChatAndType.forEach((element) -> {
            List<String> stringList = Arrays.asList(element.split(":"));
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(stringList.get(2));
            mapTypeChatMap.put(stringList.get(0), new ChatDTO(TypeChat.valueOf(stringList.get(1)), null, userDTO));
        });
        return mapTypeChatMap;
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
