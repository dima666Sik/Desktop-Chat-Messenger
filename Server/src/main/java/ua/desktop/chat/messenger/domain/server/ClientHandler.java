package ua.desktop.chat.messenger.domain.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.closable.ifaces.Closable;
import ua.desktop.chat.messenger.closable.util.ClosableImpl;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

final class ClientHandler implements Runnable {
    private final static Logger logger = LogManager.getLogger(ClientHandler.class.getName());
    private final static List<ClientHandler> clientHandlers = new ArrayList<>();
    private final Closable closable = new ClosableImpl();
    private final Socket socket;
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;
    private User user;

    private boolean userRead = false;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            clientHandlers.add(this);
        } catch (IOException e) {
            exitFromChat();
            throw new RuntimeException(e);
        }
    }

    private Message readMessage() {
        try {
            return (Message) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.user.getUsername().equals(this.user.getUsername())) {
                    clientHandler.objectOutputStream.write(message.getBytes());
                    clientHandler.objectOutputStream.flush();
                } else {
                    clientHandler.objectOutputStream.write("--> ".concat(message).getBytes());
                    clientHandler.objectOutputStream.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        Message messageFromClient = null;
        while ((messageFromClient = readMessage()) != null) {
            if (!userRead) {
                user = messageFromClient.getChat().getUser();
                userRead = true;
            }
            broadcastMessage(this.user.getUsername().concat(": ").concat(messageFromClient.getMessage()));
        }
    }

    private void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage(getClass().getSimpleName()
                .concat(": ").concat(user.getUsername()
                        .concat(" was left the chat! Goodbye!")));
    }

    public void exitFromChat() {
        removeClientHandler();
        closable.closeSocket(socket);
    }
}