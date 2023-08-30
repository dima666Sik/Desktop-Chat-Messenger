package ua.desktop.chat.messenger.main;

import ua.desktop.chat.messenger.domain.ifaces.ServerProvider;
import ua.desktop.chat.messenger.domain.server.Server;

import java.io.IOException;
import java.net.ServerSocket;

public class MainServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8086)) {
            ServerProvider serverProvider = new Server(serverSocket);
            serverProvider.startServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
