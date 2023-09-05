package ua.desktop.chat.messenger.domain;

import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private ConnectionHandler connectionHandler;
    private Socket conn;
    private Boolean isActive = true;
    private User user;
    private BufferedReader in;
    private PrintStream out;

    public ClientHandler(ConnectionHandler cHandler, Socket conn) {
        try {
            this.connectionHandler = cHandler;
            this.conn = conn;
            //get socket writing and reading streams
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            out = new PrintStream(conn.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String receivedObjectString;
        while (isActive) {
            try {
                if (user == null) {
                    out.println("Type your Username:");
                    while ((receivedObjectString = in.readLine()) != null) {
                        user = (User) ParserJSON.convertStringToObject(receivedObjectString);
                        if (connectionHandler.getClients().containsKey(user.getUsername())) {
                            System.out.println("You cannot chose this username");
                        } else if (!user.getUsername().isEmpty()) {
                            System.out.println("Username accepted");
                            System.out.println(user.getUsername());
                            connectionHandler.addClient(user.getUsername(), this);
                            break;
                        } else {
                            System.out.println("No Username chosen.");
                        }
                    }
                }

                System.out.println("+++++++++++1" + Thread.currentThread().getName());
                String userInput;

                OUTER:
                while ((userInput = in.readLine()) != null && !userInput.equals(".")) {
                    System.out.println("+++++++++++2");
                    switch (userInput) {
                        case "/M":
                            Message message;
                            System.out.println("+++++++++++3");
                            if (connectionHandler.getClients().size() > 1) {
                                String receiver;
                                while ((receiver = in.readLine()) != null) {
                                    System.out.println("SENDER / " + receiver);
                                    while ((receivedObjectString = in.readLine()) != null) {
                                        message = (Message) ParserJSON.convertStringToObject(receivedObjectString);
                                        System.out.println(user.getUsername() + " " + message.getMessage());
                                        String msgJSON = ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT);
                                        String userJSON = ParserJSON.convertObjectToString(user, TypeMessage.USER_OBJECT);
                                        connectionHandler.sendMessage(this, receiver, userJSON, msgJSON);
                                        break;
                                    }
                                    break;
                                }
                            } else {
                                System.out.println("+++++++++++4");
                                out.println("/M");
                                String msgJSON = ParserJSON.convertObjectToString("[NOBODY IS HERE]", TypeMessage.STRING_NOTIFICATION);
                                out.println(msgJSON);
                            }
                            break OUTER;
                        case "/EXIT":
                            try {
                                System.out.println("Exit!!!!!!!!!!!!!!!!!!!!!!");
                                connectionHandler.removeClient(user.getUsername());
                                conn.close();
                                isActive = false;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            } catch (SocketException se) {
                try {
                    se.printStackTrace();
                    connectionHandler.removeClient(user.getUsername());
                    conn.close();
                    isActive = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void sendUserList(ArrayList<String> ul) {
        StringBuilder userListResponse = new StringBuilder();
        out.println("/GAU");
        for (String string : ul) {
            userListResponse.append(string).append(":");
        }
        out.println(userListResponse + "[END]");
        userListResponse.delete(0, userListResponse.length());
    }

    public void sendMessage(String msg) {
        out.println("/M");
        out.println(msg);
    }

    public void terminate() {
        isActive = false;
    }

    public String getUsername() {
        return user.getUsername();
    }

}
