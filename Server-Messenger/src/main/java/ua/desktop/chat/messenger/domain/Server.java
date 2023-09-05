package ua.desktop.chat.messenger.domain;

public class Server {

    public static void main(String[] args) {
        Runnable runnableCH = new ConnectionHandler("Messenger Sever", 5000);
        Thread thread = new Thread(runnableCH);
        thread.start();
    }

}
