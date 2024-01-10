package ua.desktop.chat.messenger.client.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.client.exception.ReadBufferException;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.core.service.MessageSystemHandling;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Random;

public class CommunicationHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(CommunicationHandler.class.getName());
    private volatile boolean isActive = true;
    private PrintWriter socketOutputWriter;
    private BufferedReader socketInputReader;
    private volatile boolean isConnected = false;
    private final ChatSystemHandling chatSystemMessaging;
    private final MessageSystemHandling messageSystemHandling;


    public CommunicationHandler(ChatSystemHandling chatSystemMessaging, MessageSystemHandling messageSystemHandling) {
        this.chatSystemMessaging = chatSystemMessaging;
        this.messageSystemHandling = messageSystemHandling;
    }

    public void run() {
        try {
            Client client = new Client(this);
            Thread thread = new Thread(client);
            thread.setName("Thread-Client-" + new Random().nextInt()*10/4+4);
            thread.start();

            while (isActive) {
                if (getIsConnected()) {
                    System.out.println(Thread.currentThread());
                    client.getChatManagerProcessGUI().processResponse();
                }
            }

        } catch (Exception e) {
            logger.error("Exception. Problem with read from buffer.", e);
            throw new ReadBufferException("Exception. Problem with read from buffer.", e);
        }
    }

    public synchronized boolean getIsConnected() {
        return isConnected;
    }

    public synchronized void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public synchronized void setActive(Boolean active) {
        isActive = active;
    }

    public synchronized PrintWriter getSocketOutputWriter() {
        return socketOutputWriter;
    }

    public synchronized void setSocketOutputWriter(PrintWriter socketOutputWriter) {
        this.socketOutputWriter = socketOutputWriter;
    }

    public synchronized BufferedReader getSocketInputReader() {
        return socketInputReader;
    }

    public synchronized void setSocketInputReader(BufferedReader socketInputReader) {
        this.socketInputReader = socketInputReader;
    }

    public synchronized ChatSystemHandling getChatSystemMessaging() {
        return chatSystemMessaging;
    }

    public synchronized MessageSystemHandling getMessageSystemHandling() {
        return messageSystemHandling;
    }
}
