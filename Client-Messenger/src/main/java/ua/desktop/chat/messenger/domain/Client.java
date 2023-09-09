package ua.desktop.chat.messenger.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.auth.ui.swing.auth.AuthorizationGUI;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.ui.PreIntermediateConnectGUI;
import ua.desktop.chat.messenger.ui.chat.ChatMessengerGUI;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Client implements Runnable {
    private final static Logger logger = LogManager.getLogger(Client.class.getName());
    private String host;
    private int portNumber;
    private Socket s;
    private PrintWriter s_out;
    private BufferedReader s_in;
    private UserDTO user;
    private final CommunicationHandler communicationHandler;
    private Boolean isConnected = false;
    private ChatMessengerGUI windowChatMessenger;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public Client(CommunicationHandler cH) {
        communicationHandler = cH;
    }

    @Override
    public void run() {
        connect();
        if (isConnected) {
            logger.info("Open chat!");
            openChat();
        }
    }

    public void openChat() {
        windowChatMessenger = new ChatMessengerGUI(this);
        communicationHandler.setIsConnected(true);
        setCHConfiguration();
        windowChatMessenger.startGUI();
    }

    public void connect() {
        try {

            AuthorizationGUI authorizationGUI = new AuthorizationGUI();
            authorizationGUI.startGUI();

            while (true) {
                if (!authorizationGUI.isDisplayable()) {
                    PreIntermediateConnectGUI preIntermediateConnectGUI = new PreIntermediateConnectGUI(this);
                    preIntermediateConnectGUI.startGUI();

                    if (authorizationGUI.getUser() != null) {
                        user = new UserDTO(authorizationGUI.getUser());
                        System.out.println(user);
                        String serializedObject = ParserJSON.convertObjectToString(user, TypeMessage.USER_OBJECT);
                        s_out.println(serializedObject);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Exception when u try to connection into server!.", e);
            connect();
        }
    }

    public void tryConnect() {
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(host, portNumber));
            logger.info("Connected");
            s_out = new PrintWriter(s.getOutputStream(), true);
            s_in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            isConnected = true;
        } catch (ConnectException cE) {
            logger.error("Cannot Connect");
            throw new RuntimeException("Cannot Connect");
        } catch (UnknownHostException e) {
            logger.error("Don't know about host : " + host);
            throw new RuntimeException("Don't know about host : " + host);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void setCHConfiguration() {
        communicationHandler.setS_in(s_in);
        communicationHandler.setS_out(s_out);
        communicationHandler.setS(s);
    }

    public void updateUserListChatGUI(Set<String> users) {
        System.out.println("+-+");
        users.forEach(user -> System.out.println(user));
        windowChatMessenger.addUserList(users);
    }

    public void updateMessageChatGUI(MessageDTO msg) {
        try {
            if (msg.getChat().getTypeChat() == TypeChat.GLOBAL) {
                logger.info(msg.getMessage().substring(9, 9 + user.getUsername().length()));
                if (!msg.getMessage().substring(9, 9 + user.getUsername().length()).equals(user.getUsername())) {
                    msg.setMessage("(" + msg.getLocalDateTime().format(formatter) + ")" + msg.getMessage());
                    windowChatMessenger.updateChat(msg.getMessage());
                }
            } else {
                msg.setMessage("(" + msg.getLocalDateTime().format(formatter) + ")" + msg.getMessage());
                windowChatMessenger.updateChat(msg.getMessage());
            }

        } catch (StringIndexOutOfBoundsException e) {
            msg.setMessage("(" + msg.getLocalDateTime().format(formatter) + ")" + msg.getMessage());
            windowChatMessenger.updateChat(msg.getMessage());
        }
    }

    public void updateMessageChatGUI(String msg) {
        try {
            if (msg.startsWith("[GLOBAL]")) {
                logger.info(msg.substring(9, 9 + user.getUsername().length()));
                if (!msg.substring(9, 9 + user.getUsername().length()).equals(user.getUsername())) {
                    windowChatMessenger.updateChat(msg);
                }
            } else {
                windowChatMessenger.updateChat(msg);
            }

        } catch (StringIndexOutOfBoundsException e) {
            windowChatMessenger.updateChat(msg);
        }
    }

    public void sendMessage(MessageDTO message) {
        s_out.println("/M");
        s_out.println((message.getChat().getNameChat().isEmpty()) ? TypeChat.GLOBAL.name() : message.getChat().getNameChat());
        String msg = ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT);
        s_out.println(msg);
    }

    public void sendEXIT() {
        s_out.println("/EXIT");
    }

    public synchronized List<MessageDTO> getMessagesInChatForUser(ChatDTO chatDTO) {
        try {
            //TODO create read message from db!
            if (communicationHandler.getChatSystemMessaging().isExistChatByUser(chatDTO.getNameChat(), user.getId())) {

                List<Chat> chatORMList = communicationHandler.getChatSystemMessaging().readListChatsByChatName(chatDTO.getNameChat());

                return messageToMessageDTO(communicationHandler.getMessageSystemHandling().readListMessageByChats(chatORMList));
            } else throw new RuntimeException("Message in chat was not added!");

        } catch (Exception e) {
            throw new RuntimeException("Messages isn`t read from db!", e);
        }
    }

    private synchronized List<MessageDTO> messageToMessageDTO(List<Message> messages) {
        List<MessageDTO> messageDTOS = new ArrayList<>();
        for (Message message : messages) {
            ChatDTO chatDTO = new ChatDTO(message.getChat().getNameChat(), message.getChat().getTypeChat(), user);
            messageDTOS.add(new MessageDTO(message.getMessage(), message.getLocalDateTime(), chatDTO));
        }
        return messageDTOS;
    }

    public synchronized Boolean getIsConnected() {
        return isConnected;
    }

    public synchronized void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public synchronized CommunicationHandler getCommunicationHandler() {
        return communicationHandler;
    }

    public synchronized UserDTO getUser() {
        return user;
    }

    public synchronized String getHost() {
        return host;
    }

    public synchronized void setHost(String host) {
        this.host = host;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
}
