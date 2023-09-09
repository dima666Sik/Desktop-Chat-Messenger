package ua.desktop.chat.messenger.domain.ifaces;

import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;

import java.util.List;

public interface MessageSystemHandling {
    Message createMessageByChat(Message message);
    List<Message> readListMessageByChats(List<Chat> chatList);
}
