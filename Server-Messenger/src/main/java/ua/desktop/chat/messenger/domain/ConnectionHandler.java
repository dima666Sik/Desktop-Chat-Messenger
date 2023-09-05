package ua.desktop.chat.messenger.domain;

import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.IOException;
import java.io.PrintStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ConnectionHandler implements Runnable {
    private HashMap<String, ClientHandler> clients = new HashMap<>();
    private final ArrayList<String> userList = new ArrayList<>();
    private Boolean isActive = true;
    private Boolean newUser = true;
    private ServerSocket s;
    private final int portNumber;
    private final String serverName;

    public ConnectionHandler(String serverName, int portNumber) {
        this.portNumber = portNumber;
        this.serverName = serverName;
    }

    public void run() {
        while (isActive) {
            try {
                //1. creating a server socket - 1st parameter is port number and 2nd is the backlog
                InetAddress addr = InetAddress.getLocalHost();

                s = new ServerSocket(portNumber, 10, addr);
                s.setReuseAddress(true);
                System.out.println("InetAddress : " + s.getInetAddress());

                while (!s.isClosed()) {
                    if (newUser) {
                        newUser = false;
                    } else {
                        //get the connection socket
                        Socket conn = s.accept();

                        System.out.println("Connection received from " + conn.getInetAddress().getHostName() + " : " + conn.getPort());

                        //create new thread to handle client
                        Runnable runnableCH = new ClientHandler(this, conn);
                        Thread thread = new Thread(runnableCH);
                        thread.start();
                    }
                }
            } catch (IOException e) {
                System.err.println("IOException".concat(String.valueOf(e)));
            }

            try {
                s.close();
            } catch (IOException e) {
                System.err.println("Unable to close. IOException".concat(String.valueOf(e)));
            }
        }
    }

    public void test(PrintStream output) {
        output.println("TEST");
    }

    public void sendMessage(ClientHandler sender, String clientRCVR, String userJSON, String msgJSON) {

        User user = (User) ParserJSON.convertStringToObject(userJSON);
        Message message = (Message) ParserJSON.convertStringToObject(msgJSON);

        if (clients.size() <= 1) {
            message.setMessage("[MESSAGE COULD NOT BE SEND!]");
            sender.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));
            return;
        }


        System.out.println("-----------------" + clientRCVR + " " + message.getChat().getTypeChat().name());
        if (clientRCVR.equals("GLOBAL")) {
            message.setMessage("[".concat(message.getChat().getTypeChat().name()).concat("] ").concat(user.getUsername()).concat(": ").concat(message.getMessage()));
            for (String key : clients.keySet()) {
                ClientHandler client = clients.get(key);
                client.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));
            }
        } else {
            try {
                System.out.println("Contain PLZ: " + clients.containsKey(clientRCVR));
                if (clients.containsKey(clientRCVR)) {
                    message.setMessage("[".concat(message.getChat().getTypeChat().name()).concat("] ").concat(user.getUsername()).concat(": ").concat(message.getMessage()));
                    clients.get(clientRCVR).sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));
                } else {
                    message.setMessage("[MESSAGE COULD NOT BE SEND!]");
                    sender.sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void addClient(String username, ClientHandler ch) {
        clients.put(username, ch);
        informAllClientsUserList();
    }

    public synchronized void removeClient(String username) {
        clients.remove(username);
        informAllClientsUserList();
    }

    public void informAllClientsUserList() {
        fillUserList();
        for (String key : clients.keySet()) {
            ClientHandler client = clients.get(key);
            client.sendUserList(userList);
        }
    }

    private void fillUserList() {
        userList.clear();
        for (String key : clients.keySet()) {
            ClientHandler client = clients.get(key);
            userList.add(client.getUsername());
        }
    }

    public void terminate() {
        isActive = false;
    }

    public synchronized HashMap<String, ClientHandler> getClients() {
        return clients;
    }

    public synchronized void setClients(HashMap<String, ClientHandler> clients) {
        this.clients = clients;
    }


}
