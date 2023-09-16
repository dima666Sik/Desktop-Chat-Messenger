package ua.desktop.chat.messenger.domain;

import com.google.common.collect.Multimap;
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
import java.util.Optional;

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
                        user = authorizationGUI.getUser();
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

    public void updateUserListChatGUI(Multimap<String, ChatDTO> chats) {
        windowChatMessenger.addChatsList(chats);
    }

    public void updateMessageChatGUI(MessageDTO msg) {
        System.out.println("---0" + msg);
        if (msg.getChat().getTypeChat() == TypeChat.GLOBAL || msg.getChat().getTypeChat() == TypeChat.GROUP) {
            logger.info(msg.getMessage().substring(9, 9 + user.getUsername().length()));
            if (!msg.getMessage().substring(9, 9 + user.getUsername().length()).equals(user.getUsername())) {
                msg.setMessage("(" + msg.getLocalDateTime().format(formatter) + ")" + msg.getMessage());
                windowChatMessenger.updateChat(msg);
            }
        } else {
            msg.setMessage("(" + msg.getLocalDateTime().format(formatter) + ")" + msg.getMessage());
            windowChatMessenger.updateChat(msg);
        }
    }

    public void updateMessageChatGUI(String msg) {
        windowChatMessenger.updateChat(msg);
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
                List<ChatDTO> chatORMList = new ArrayList<>();
                if (chatDTO.getTypeChat() == TypeChat.PRIVATE) {
                    Optional<ChatDTO> chatOwner = communicationHandler.getChatSystemMessaging().readChat(chatDTO.getNameChat(), user.getId());
                    if (chatOwner.isEmpty()) throw new RuntimeException("Chat is not found!");
                    chatORMList.add(chatOwner.get());

                    Optional<ChatDTO> chatCompanion = communicationHandler.getChatSystemMessaging().readChatCompanion(chatOwner.get());
                    if (chatCompanion.isEmpty()) throw new RuntimeException("Chat Companion is not found!");
                    chatORMList.add(chatCompanion.get());
                } else {

                    Optional<List<ChatDTO>> chatDTOList = communicationHandler.getChatSystemMessaging().readListChatsByChatName(chatDTO.getNameChat());
                    if (chatDTOList.isEmpty()) throw new RuntimeException("Chats is not found!");
                    chatORMList = chatDTOList.get();
                }
                Optional<List<MessageDTO>> optionalMessageDTOList = communicationHandler.getMessageSystemHandling().readListMessageByChats(chatORMList);
                if (optionalMessageDTOList.isEmpty()) throw new RuntimeException("Message not found for this chat!");
                return optionalMessageDTOList.get();
            } else throw new RuntimeException("Message in chat was not added!");

        } catch (Exception e) {
            throw new RuntimeException("Messages isn`t read from db!", e);
        }
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
