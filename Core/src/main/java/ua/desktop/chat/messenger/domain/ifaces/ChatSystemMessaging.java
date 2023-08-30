package ua.desktop.chat.messenger.domain.ifaces;

import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;

public interface ChatSystemMessaging {
    boolean isExistChatByUser(String nameChat, User user);

    Chat createChatByUser(String nameChat, TypeChat typeChat, User user);

    List<Chat> readListChatsByUser(User user);
}
