package ua.desktop.chat.messenger.client.service;

import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.auth.ui.swing.auth.AuthorizationGUI;
import ua.desktop.chat.messenger.client.exception.*;
import ua.desktop.chat.messenger.client.ui.PreIntermediateConnectGUI;
import ua.desktop.chat.messenger.client.ui.chat.ChatMessengerGUI;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;

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
    private static final Logger logger = LogManager.getLogger(Client.class.getName());
    private String host;
    private int portNumber;
    private Socket socket;
    private PrintWriter socketOutputWriter;
    private BufferedReader socketInputReader;
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
        AuthorizationGUI authorizationGUI = new AuthorizationGUI();
        authorizationGUI.startGUI();

        while (true) {
            if (!authorizationGUI.isDisplayable()) {
                PreIntermediateConnectGUI preIntermediateConnectGUI = new PreIntermediateConnectGUI(this);
                preIntermediateConnectGUI.startGUI();

                if (authorizationGUI.getUser() != null) {
                    user = authorizationGUI.getUser();
                    String serializedObject = ParserJSON.convertObjectToString(user, TypeMessage.USER_OBJECT);
                    socketOutputWriter.println(serializedObject);
                }
                break;
            }
        }
    }

    public void tryConnect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, portNumber));
            logger.info("Connected");
            socketOutputWriter = new PrintWriter(socket.getOutputStream(), true);
            socketInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;
        } catch (ConnectException e) {
            logger.error("Cannot Connect", e);
            throw new ConnectionException("Cannot Connect", e);
        } catch (UnknownHostException e) {
            logger.error("Don't know about host : {}", host, e);
            throw new UnsuccessfulFoundHostException("Don't know about host : " + host, e);
        } catch (IOException e) {
            throw new SocketInitException("Exception when u try to connect into server!", e);
        }
    }

    public synchronized void setCHConfiguration() {
        communicationHandler.setSocketInputReader(socketInputReader);
        communicationHandler.setSocketOutputWriter(socketOutputWriter);
        communicationHandler.setSocket(socket);
    }

    public void updateUserListChatGUI(Multimap<String, ChatDTO> chats) {
        windowChatMessenger.addChatList(chats);
    }

    public void updateMessageChatGUI(MessageDTO msg) {

        if (msg.getChat().getTypeChat() == TypeChat.GLOBAL || msg.getChat().getTypeChat() == TypeChat.GROUP) {
            if (!msg.getMessage().substring(9, 9 + user.getUsername().length()).equals(user.getUsername())) {
                setMessage(msg);
            }
        } else {
            setMessage(msg);
        }
    }

    private void setMessage(MessageDTO msg) {
        msg.setMessage(String.format("(%s)%s", msg.getLocalDateTime().format(formatter), msg.getMessage()));
        windowChatMessenger.updateChat(msg);
    }

    public void updateMessageChatGUI(String msg) {
        windowChatMessenger.updateChat(msg);
    }

    public void sendMessage(MessageDTO message) {
        socketOutputWriter.println("/M");
        socketOutputWriter.println((message.getChat().getNameChat().isEmpty()) ? TypeChat.GLOBAL.name() : message.getChat().getNameChat());
        String msg = ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT);
        socketOutputWriter.println(msg);
    }

    public void sendEXIT() {
        socketOutputWriter.println("/EXIT");
    }

    public void sendUpdateGroupIntoList() {
        socketOutputWriter.println("/UPDATE GROUP INTO LIST");
    }

    private void validateChatExistence(ChatDTO chatDTO) {
        if (!communicationHandler.getChatSystemMessaging().isExistChatByUser(chatDTO.getNameChat(), user.getId())) {
            throw new AddMessageException("Message in chat was not added!");
        }
    }

    public synchronized List<MessageDTO> getMessagesInChatForUser(ChatDTO chatDTO) {
        validateChatExistence(chatDTO);
        List<ChatDTO> chatORMList = getChatList(chatDTO);
        return getMessageList(chatORMList);
    }

    private List<ChatDTO> getChatList(ChatDTO chatDTO) {
        List<ChatDTO> chatORMList = new ArrayList<>();
        if (chatDTO.getTypeChat() == TypeChat.PRIVATE) {
            Optional<ChatDTO> chatOwner = communicationHandler.getChatSystemMessaging().readChat(chatDTO.getNameChat(), user.getId());
            chatORMList.add(chatOwner.orElseThrow(() -> new UndefinedChatException("Chat is not found!")));

            Optional<ChatDTO> chatCompanion = communicationHandler.getChatSystemMessaging().readChatCompanion(chatOwner.get());
            chatORMList.add(chatCompanion.orElseThrow(() -> new UndefinedChatException("Chat Companion is not found!")));
        } else {
            List<ChatDTO> chatDTOList = communicationHandler
                    .getChatSystemMessaging()
                    .readListChatsByChatName(chatDTO.getNameChat());
            if (chatDTOList.isEmpty()) throw new UndefinedChatException("Chats is not found!");
            chatORMList = chatDTOList;
        }
        return chatORMList;
    }

    private List<MessageDTO> getMessageList(List<ChatDTO> chatORMList) {
        List<MessageDTO> optionalMessageDTOList = communicationHandler.getMessageSystemHandling().readListMessageByChats(chatORMList);
        if (optionalMessageDTOList.isEmpty()) throw new UndefinedMessageException("Message not found for this chat!");
        return optionalMessageDTOList;
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
