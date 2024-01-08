package ua.desktop.chat.messenger.server.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.server.service.chat.ChatExitHandler;
import ua.desktop.chat.messenger.server.service.chat.ChatHandler;
import ua.desktop.chat.messenger.server.service.chat.ChatHandlerGUI;
import ua.desktop.chat.messenger.server.service.chat.UserInitializer;
import ua.desktop.chat.messenger.server.service.exception.SocketClosedException;
import ua.desktop.chat.messenger.server.service.exception.SocketInitException;
import ua.desktop.chat.messenger.server.service.message.MessageHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class.getName());
    private final ConnectionHandler connectionHandler;
    private final Socket socket;
    private final BufferedReader socketInputReader;
    private final PrintStream socketOutputWriter;
    private volatile boolean isActive = true;
    private UserInitializer initializeUser;
    private MessageHandler messageHandler;
    private ChatHandler chatHandler;
    private ChatExitHandler processExitUser;
    private ChatHandlerGUI chatHandlerGUI;


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
            initializeUser = new UserInitializer(this, connectionHandler);
            messageHandler = new MessageHandler(this, connectionHandler);
            chatHandler = new ChatHandler(this, connectionHandler);
            processExitUser = new ChatExitHandler(this, connectionHandler);
            chatHandlerGUI = new ChatHandlerGUI(this, connectionHandler);
            try {
                initializeUser.initializeUser();
                chatHandler.processInputChoice();
            } catch (SocketException e) {
                processExitUser.exitUser();
            } catch (Exception e) {
                logger.error("Unable to exit from clientHandler, problem with close Socket.", e);
                throw new SocketClosedException("Unable to exit from clientHandler, problem with close Socket.", e);
            }
        }
    }

    public synchronized ChatHandlerGUI getChatManagerProcessGUI() {
        return chatHandlerGUI;
    }

    public ChatExitHandler getProcessExitUser() {
        return processExitUser;
    }

    public synchronized boolean isActive() {
        return isActive;
    }

    public synchronized void setActive(boolean active) {
        isActive = active;
    }

    public synchronized Socket getSocket() {
        return socket;
    }
    public synchronized BufferedReader getSocketInputReader() {
        return socketInputReader;
    }

    public synchronized PrintStream getSocketOutputWriter() {
        return socketOutputWriter;
    }

    public synchronized UserInitializer getInitializeUser() {
        return initializeUser;
    }

    public synchronized MessageHandler getMessageManager() {
        return messageHandler;
    }

    public synchronized ChatHandler getChatManager() {
        return chatHandler;
    }
}
