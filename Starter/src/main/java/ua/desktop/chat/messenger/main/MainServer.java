package ua.desktop.chat.messenger.main;

import ua.desktop.chat.messenger.server.domain.ConnectionHandler;
import ua.desktop.chat.messenger.core.domain.impl.ChatSystemHandlerImpl;
import ua.desktop.chat.messenger.core.domain.impl.MessageSystemHandlerImpl;

public class MainServer {
    public static void main(String[] args) {
        Runnable runnableCH = new ConnectionHandler(new ChatSystemHandlerImpl(), new MessageSystemHandlerImpl());
        Thread thread = new Thread(runnableCH);
        thread.start();
    }

}
