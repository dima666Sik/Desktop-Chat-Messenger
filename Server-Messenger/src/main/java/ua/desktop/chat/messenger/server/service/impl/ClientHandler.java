package ua.desktop.chat.messenger.server.service.impl;

import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.server.service.ClientObserver;
import ua.desktop.chat.messenger.server.service.exception.SocketClosedException;
import ua.desktop.chat.messenger.server.service.exception.SocketInitException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

public class ClientHandler implements Runnable, ClientObserver {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class.getName());
    private final ConnectionHandler connectionHandler;
    private final Socket socket;
    private volatile boolean isActive = true;
    private UserDTO userDTO;
    private final BufferedReader socketInputReader;
    private final PrintStream socketOutputWriter;

    public ClientHandler(ConnectionHandler cHandler, Socket socket) {
        try {
            this.connectionHandler = cHandler;
            this.socket = socket;
            //get socket writing and reading streams
            socketInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOutputWriter = new PrintStream(socket.getOutputStream(), true);
        } catch (IOException e) {
            logger.error("Unable to init streams in clientHandler", e);
            throw new SocketInitException("Unable to init streams in clientHandler", e);
        }
    }

    public void run() {
        while (isActive) {
            try {
                initializeUser();
                processInputChoice();
            } catch (SocketException e) {
                processingExitUser();
            } catch (Exception e) {
                logger.error("Unable to exit from clientHandler, problem with close Socket.", e);
                throw new SocketClosedException("Unable to exit from clientHandler, problem with close Socket.", e);
            }
        }
    }

    private void processInputChoice() throws IOException {
        String userInput;
        while ((userInput = socketInputReader.readLine()) != null) {
            switch (userInput) {
                case "/M":
                    processUserMessage();
                    break;
                case "/UPDATE GROUP INTO LIST":
                    connectionHandler.informAllClientsUserNameList();
                    break;
                case "/EXIT":
                    processExitCommand();
                    break;
            }
        }
    }

    private void processUserMessage() throws IOException {
        String receivedObjectString;
        MessageDTO messageDTO;
        if (connectionHandler.getClientHandlers().size() > 1) {
            String receiver;
            if ((receiver = socketInputReader.readLine()) != null) {
                logger.info("SENDER / {}", receiver);
                if ((receivedObjectString = socketInputReader.readLine()) != null) {
                    messageDTO = (MessageDTO) ParserJSON.convertStringToObject(receivedObjectString);
                    logger.info("userName:  {}, message: {}", userDTO.getUsername(), messageDTO.getMessage());

                    String msgJSON = ParserJSON.convertObjectToString(messageDTO, TypeMessage.MESSAGE_OBJECT);
                    String userJSON = ParserJSON.convertObjectToString(userDTO, TypeMessage.USER_OBJECT);

                    connectionHandler.sendMessage(this, receiver, userJSON, msgJSON);
                    connectionHandler.sendMessageInDB(receiver, userDTO, messageDTO);
                }
            }
        } else {
            socketOutputWriter.println("/M");
            String msgJSON = ParserJSON.convertObjectToString("[NOBODY IS HERE. YOUR MESSAGES NOT SAVED!]", TypeMessage.STRING_NOTIFICATION);
            socketOutputWriter.println(msgJSON);

            logger.info("NOBODY IS HERE. YOUR MESSAGES NOT SAVED! this message for once user in chat: {}", userDTO.getUsername());
            connectionHandler.getServerGUI().updateChat("NOBODY IS HERE. YOUR MESSAGES NOT SAVED! this message for once user in chat: " + userDTO.getUsername());
        }
    }

    private void processExitCommand() throws IOException {
        connectionHandler.removeClient(userDTO.getUsername(), new ChatDTO(TypeChat.PRIVATE, userDTO.getId(), userDTO));

        logger.info("Client was removed from the list chat! Name current client is: {}", userDTO.getUsername());
        connectionHandler.getServerGUI().updateChat("Client was removed from the list chat! Name current client is: " + userDTO.getUsername());

        socket.close();
        isActive = false;
    }

    private void processingExitUser() {
        try {
            connectionHandler.removeClient(userDTO.getUsername(), new ChatDTO(TypeChat.PRIVATE, userDTO.getId(), userDTO));
            socket.close();
            isActive = false;
            logger.warn("Socket was closed. U successful exit from client!");
        } catch (IOException e) {
            logger.error("SocketException. Problem with closed Socket.", e);
            throw new SocketClosedException("SocketException. Problem with closed Socket.", e);
        }
    }

    private void initializeUser() throws IOException {
        String receivedObjectString;
        if (userDTO == null) {
            while ((receivedObjectString = socketInputReader.readLine()) != null) {
                userDTO = (UserDTO) ParserJSON.convertStringToObject(receivedObjectString);
                if (connectionHandler.getClientHandlers().containsKey(userDTO.getUsername())) {
                    logger.info("You cannot chose this username");
                } else if (!userDTO.getUsername().isEmpty()) {
                    logger.info("Username accepted: {}", userDTO.getUsername());
                    connectionHandler.addClient(userDTO.getUsername(), this);
                    break;
                } else {
                    logger.info("No Username chosen.");
                }
            }
        }
    }

    @Override
    public void sendUserNameList(Multimap<String, ChatDTO> ul) {
        StringBuilder userListResponse = new StringBuilder();
        socketOutputWriter.println("/USERS");

        for (Map.Entry<String, ChatDTO> entry : ul.entries()) {
            String userChat = entry.getKey();
            ChatDTO chatDTO = entry.getValue();

            // Add chats with current names into db if them not exist there
            userListResponse.append(userChat)
                    .append(":").append(chatDTO.getTypeChat())
                    .append(":").append(chatDTO.getUser().getUsername()).append(",");

            connectionHandler.createChatsIfNotExist(userChat, chatDTO, userDTO);
        }
        socketOutputWriter.println(userListResponse);
    }

    @Override
    public void sendMessage(String msg) {
        socketOutputWriter.println("/M");
        socketOutputWriter.println(msg);
    }

    public String getUsername() {
        return userDTO.getUsername();
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

}
