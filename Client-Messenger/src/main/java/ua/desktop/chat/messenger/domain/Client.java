package ua.desktop.chat.messenger.domain;

import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.exception.DomainClientExceptionUI;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.ui.PreIntermediateConnectUI;
import ua.desktop.chat.messenger.ui.chat.ChatMessenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

public class Client implements Runnable {
    private String host = "DESKTOP-OCNPFLI";
    private int portNumber = 5000;
    private Socket s;
    private PrintWriter s_out = null;
    private BufferedReader s_in = null;
    private User user;
    private final CommunicationHandler communicationHandler;
    private Boolean isConnected = false;
    public ChatMessenger windowChatMessenger;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public Client(CommunicationHandler cH) {
        communicationHandler = cH;
    }

    @Override
    public void run() {
        try {
            connect();
            if (isConnected) {
                System.out.println("open chat!");
                openChat();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openChat() {
        try {
            windowChatMessenger = new ChatMessenger(this);
            communicationHandler.setIsConnected(true);
            setCHConfiguration();
            System.out.println("_+_+_+_+_+ " + getIsConnected());
            windowChatMessenger.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            new PreIntermediateConnectUI(this);
            if (user != null) {
                String serializedObject = ParserJSON.convertObjectToString(user, TypeMessage.USER_OBJECT);
                s_out.println(serializedObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            connect();
        }
    }

    public void tryConnect() {
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(host, portNumber));
            System.out.println("Connected");
            //writer for socket
            s_out = new PrintWriter(s.getOutputStream(), true);
            //reader for socket
            s_in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            isConnected = true;
        } catch (ConnectException cE) {
            System.err.println("Cannot Connect");
            new DomainClientExceptionUI("Cannot Connect");
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host : " + host);
            new DomainClientExceptionUI("Don't know about host : " + host);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCHConfiguration() {
        communicationHandler.setS_in(s_in);
        communicationHandler.setS_out(s_out);
        communicationHandler.setS(s);
    }

    public void updateUserListChatGUI(ArrayList<String> users) {
        windowChatMessenger.addUserList(users);
    }

    public void updateMessageChatGUI(Message msg) {
        try {
            System.out.println("msg:" + msg);
            if (msg.getChat().getTypeChat() == TypeChat.GLOBAL) {
                System.out.println(msg.getMessage().substring(9, 9 + user.getUsername().length()));
                if (!msg.getMessage().substring(9, 9 + user.getUsername().length()).equals(user.getUsername())) {
                    msg.setMessage("(" + msg.getLocalDateTime().format(formatter) + ")" + msg.getMessage());
                    System.out.println(msg.getMessage());
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
                System.out.println(msg.substring(9, 9 + user.getUsername().length()));
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

    public void sendMessage(Message message) {
        s_out.println("/M");
        s_out.println((message.getChat().getNameChat().isEmpty()) ? TypeChat.GLOBAL.name() : message.getChat().getNameChat());
        String msg = ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT);
        s_out.println(msg);
    }

    public void sendEXIT() {
        s_out.println("/EXIT");
    }

    public synchronized Boolean getIsConnected() {
        return isConnected;
    }

    public synchronized void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
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

    public synchronized CommunicationHandler getCommunicationHandler() {
        return communicationHandler;
    }
//    public synchronized String getUsername() {
//        return username;
//    }
//
//    public synchronized void setUsername(String username) {
//        this.username = username;
//    }

    public synchronized User getUser() {
        return user;
    }

    public synchronized void setUser(User user) {
        this.user = user;
    }
}
