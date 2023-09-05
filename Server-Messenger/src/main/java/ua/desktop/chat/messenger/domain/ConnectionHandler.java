package ua.desktop.chat.messenger.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionHandler implements Runnable {
    private final static Logger logger = LogManager.getLogger(ConnectionHandler.class.getName());
    private Map<String, ClientHandler> clientHandlers = new HashMap<>();
    private final List<String> userNameList = new ArrayList<>();
    private Boolean isActive = true;
    private Boolean newUser = true;
    private final int portNumber;

    public ConnectionHandler(int portNumber) {
        this.portNumber = portNumber;
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

        if (clientRCVR.equals("GLOBAL")) {
            message.setMessage("[".concat(message.getChat().getTypeChat().name()).concat("] ").concat(user.getUsername()).concat(": ").concat(message.getMessage()));
            System.out.println(message.getMessage());
            for (String key : clientHandlers.keySet()) {
                ClientHandler client = clientHandlers.get(key);
                System.out.println(client.getUsername());
                client.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));
            }
        } else {
            try {
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

    public synchronized void addClient(String username, ClientHandler ch) {
        clientHandlers.put(username, ch);
        informAllClientsUserNameList();
    }

    public synchronized void removeClient(String username) {
        clientHandlers.remove(username);
        informAllClientsUserNameList();
    }

    public void informAllClientsUserNameList() {
        fillUserNameList();
        for (String key : clientHandlers.keySet()) {
            ClientHandler client = clientHandlers.get(key);
            client.sendUserNameList(userNameList);
        }
    }

    private void fillUserNameList() {
        userNameList.clear();
        for (String key : clientHandlers.keySet()) {
            ClientHandler client = clientHandlers.get(key);
            userNameList.add(client.getUsername());
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


}
