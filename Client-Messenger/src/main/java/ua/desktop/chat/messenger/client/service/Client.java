package ua.desktop.chat.messenger.client.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.auth.ui.swing.auth.AuthorizationGUI;
import ua.desktop.chat.messenger.client.exception.*;
import ua.desktop.chat.messenger.client.service.chat.ChatHandler;
import ua.desktop.chat.messenger.client.service.chat.ChatHandlerGUI;
import ua.desktop.chat.messenger.client.service.message.MessageHandler;
import ua.desktop.chat.messenger.client.service.message.MessageHandlerGUI;
import ua.desktop.chat.messenger.client.ui.PreIntermediateConnectGUI;
import ua.desktop.chat.messenger.client.ui.chat.ChatMessengerGUI;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
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

public class Client implements Runnable {
    private static final Logger logger = LogManager.getLogger(Client.class.getName());
    private final CommunicationHandler communicationHandler;
    private String host;
    private int portNumber;
    private Socket socket;
    private PrintWriter socketOutputWriter;
    private BufferedReader socketInputReader;
    private UserDTO user;
    private Boolean isConnected = false;
    private ChatMessengerGUI windowChatMessenger;
    private MessageHandler messageHandler;
    private ChatHandlerGUI chatHandlerGUI;
    private ChatHandler chatHandler;

    public Client(CommunicationHandler cH) {
        communicationHandler = cH;
    }

    @Override
    public void run() {
        connect();
        if (isConnected) {
            initializeProcessChatsAndMessages();
            openChat();
        }
    }

    private void initializeProcessChatsAndMessages() {
        chatHandler = new ChatHandler(communicationHandler, user);
        messageHandler = new MessageHandler(chatHandler);
        logger.info("Initialize process chats and messages!");
    }

    private void initializeProcessChatsGUIAndMessagesGUI(){
        MessageHandlerGUI messageHandlerGUI = new MessageHandlerGUI(windowChatMessenger);
        chatHandlerGUI = new ChatHandlerGUI(messageHandlerGUI, windowChatMessenger, chatHandler);
    }

    public void openChat() {
        windowChatMessenger = new ChatMessengerGUI(this);
        initializeProcessChatsGUIAndMessagesGUI();
        communicationHandler.setIsConnected(true);
        setCHConfiguration();
        windowChatMessenger.startGUI();
        logger.info("Open chat!");
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

    public synchronized int getPortNumber() {
        return portNumber;
    }

    public synchronized void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public synchronized MessageHandler getMessageManager() {
        return messageHandler;
    }

    public synchronized ChatHandlerGUI getChatManagerProcessGUI() {
        return chatHandlerGUI;
    }

}
