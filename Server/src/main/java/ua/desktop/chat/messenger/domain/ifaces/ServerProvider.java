package ua.desktop.chat.messenger.domain.ifaces;

public interface ServerProvider {
    void startServer();
    void closeServerSocket();
}
