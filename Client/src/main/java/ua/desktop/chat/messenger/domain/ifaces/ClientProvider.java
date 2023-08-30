package ua.desktop.chat.messenger.domain.ifaces;

import ua.desktop.chat.messenger.models.Message;

public interface ClientProvider {
    void sendMessage(Message message);
    void listenMessageFromClients();
}
