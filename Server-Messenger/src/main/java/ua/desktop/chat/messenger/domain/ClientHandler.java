package ua.desktop.chat.messenger.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private final static Logger logger = LogManager.getLogger(ConnectionHandler.class.getName());
    private final ConnectionHandler connectionHandler;
    private final Socket conn;
    private Boolean isActive = true;
    private UserDTO user;
    private final BufferedReader in;
    private final PrintStream out;

    public ClientHandler(ConnectionHandler cHandler, Socket conn) {
        try {
            this.connectionHandler = cHandler;
            this.conn = conn;
            //get socket writing and reading streams
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            out = new PrintStream(conn.getOutputStream(), true);
        } catch (Exception e) {
            logger.error("Unable to init streams in clientHandler", e);
            throw new RuntimeException("Unable to init streams in clientHandler", e);
        }
    }

    public void run() {
        String receivedObjectString, userInput;
        while (isActive) {
            try {
                if (user == null) {
                    while ((receivedObjectString = in.readLine()) != null) {
                        user = (UserDTO) ParserJSON.convertStringToObject(receivedObjectString);
                        if (connectionHandler.getClientHandlers().containsKey(user.getUsername())) {
                            logger.info("You cannot chose this username");
                        } else if (!user.getUsername().isEmpty()) {
                            logger.info("Username accepted: ".concat(user.getUsername()));
                            connectionHandler.addClient(user.getUsername(), this);
                            break;
                        } else {
                            logger.info("No Username chosen.");
                        }
                    }
                }

                OUTER:
                while ((userInput = in.readLine()) != null) {
                    switch (userInput) {
                        case "/M":
                            MessageDTO message;
                            if (connectionHandler.getClientHandlers().size() > 1) {
                                String receiver;
                                if ((receiver = in.readLine()) != null) {
                                    logger.info("SENDER / " + receiver);
                                    if ((receivedObjectString = in.readLine()) != null) {
                                        message = (MessageDTO) ParserJSON.convertStringToObject(receivedObjectString);
                                        logger.info("userName:  ".concat(user.getUsername()).concat(", message: ").concat(message.getMessage()));
                                        System.out.println(message);
                                        String msgJSON = ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT);
                                        String userJSON = ParserJSON.convertObjectToString(user, TypeMessage.USER_OBJECT);
                                        //2. TODO Send message in db for need chat for user
                                        connectionHandler.sendMessage(this, receiver, userJSON, msgJSON);
                                    }
                                    break;
                                }
                            } else {
                                out.println("/M");
                                String msgJSON = ParserJSON.convertObjectToString("[NOBODY IS HERE]", TypeMessage.STRING_NOTIFICATION);
                                out.println(msgJSON);
                            }
                            break OUTER;
                        case "/EXIT":
                            connectionHandler.removeClient(user.getUsername());
                            conn.close();
                            isActive = false;
                            break;
                    }
                }
            } catch (SocketException e) {
                try {
                    connectionHandler.removeClient(user.getUsername());
                    conn.close();
                    isActive = false;
                    logger.warn("Socket was closed. U successful exit from client!");
                } catch (IOException e1) {
                    logger.error("SocketException. Problem with closed Socket.", e1);
                    throw new RuntimeException("SocketException. Problem with closed Socket.", e1);
                }
            } catch (Exception e) {
                logger.error("Unable to exit from clientHandler, problem with close Socket.", e);
                throw new RuntimeException("Unable to exit from clientHandler, problem with close Socket.", e);
            }
        }

    }

    public void sendUserNameList(List<String> ul) {
        StringBuilder userListResponse = new StringBuilder();
        out.println("/USERS");
        for (String string : ul) {
            userListResponse.append(string).append(",");
        }
        out.println(userListResponse);
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
