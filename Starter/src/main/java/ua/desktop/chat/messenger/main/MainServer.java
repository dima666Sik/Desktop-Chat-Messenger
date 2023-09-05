package ua.desktop.chat.messenger.main;

import ua.desktop.chat.messenger.domain.ConnectionHandler;

public class MainServer {

    public static void main(String[] args) {
        Runnable runnableCH = new ConnectionHandler( 5000);
        Thread thread = new Thread(runnableCH);
        thread.start();
    }

}
