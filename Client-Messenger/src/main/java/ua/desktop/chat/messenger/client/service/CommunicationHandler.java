package ua.desktop.chat.messenger.client.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.client.exception.ReadBufferException;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.core.service.MessageSystemHandling;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class CommunicationHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(CommunicationHandler.class.getName());
    private volatile boolean isActive = true;
    private Socket socket;
    private PrintWriter socketOutputWriter;
    private BufferedReader socketInputReader;
    private volatile boolean isConnected = false;
    private final ChatSystemHandling chatSystemMessaging;
    private final MessageSystemHandling messageSystemHandling;
    private Client client;

    public CommunicationHandler(ChatSystemHandling chatSystemMessaging, MessageSystemHandling messageSystemHandling) {
        this.chatSystemMessaging = chatSystemMessaging;
        this.messageSystemHandling = messageSystemHandling;
    }

    public void run() {
        try {
            client = new Client(this);
            Thread thread = new Thread(client);
            thread.start();

            while (isActive) {
                if (getIsConnected()) {
                    processResponse();
                }
            }
        } catch (Exception e) {
            logger.error("Exception. Problem with read from buffer.", e);
            throw new ReadBufferException("Exception. Problem with read from buffer.", e);
        }
    }

    private void processResponse() throws IOException {
        String response;
        while (socketInputReader != null && (response = socketInputReader.readLine()) != null) {
            logger.info("Response: {}", response);

            if (response.equals("/USERS")) {
                processUserListResponse();
            } else if (response.equals("/M")) {
                processMessageResponse();
            }
        }
    }

    private void processUserListResponse() throws IOException {
        String userList;
        if ((userList = socketInputReader.readLine()) != null) {
            Multimap<String, ChatDTO> users = setUserList(userList);
            client.updateUserListChatGUI(users);
        }
    }

    private void processMessageResponse() throws IOException {
        String messageResponse;
        if ((messageResponse = socketInputReader.readLine()) != null) {
            Object parsedObject = ParserJSON.convertStringToObject(messageResponse);

            if (parsedObject instanceof MessageDTO) {
                MessageDTO message = (MessageDTO) parsedObject;
                client.updateMessageChatGUI(message);
            } else if (parsedObject instanceof String) {
                String textMessage = (String) parsedObject;
                client.updateMessageChatGUI(textMessage);
            }
        }
    }

    public Multimap<String, ChatDTO> setUserList(String rspUserList) {
        Set<String> strChatAndType = new HashSet<>(Arrays.asList(rspUserList.split(",")));
        Multimap<String, ChatDTO> mapTypeChatMap = ArrayListMultimap.create();
        strChatAndType.forEach(element -> {
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

    public synchronized Socket getSocket() {
        return socket;
    }

    public synchronized void setSocket(Socket socket) {
        this.socket = socket;
    }

    public synchronized PrintWriter getSocketOutputWriter() {
        return socketOutputWriter;
    }

    public synchronized void setSocketOutputWriter(PrintWriter socketOutputWriter) {
        this.socketOutputWriter = socketOutputWriter;
    }

    public synchronized BufferedReader getSocketInputReader() {
        return socketInputReader;
    }

    public synchronized void setSocketInputReader(BufferedReader socketInputReader) {
        this.socketInputReader = socketInputReader;
    }

    public synchronized ChatSystemHandling getChatSystemMessaging() {
        return chatSystemMessaging;
    }

    public synchronized MessageSystemHandling getMessageSystemHandling() {
        return messageSystemHandling;
    }
}
