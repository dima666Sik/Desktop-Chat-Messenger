package ua.desktop.chat.messenger.domain.ifaces;

import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;

public interface ChatSystemHandling {
    boolean isExistChatByUser(String nameChat, Long userId);

    Chat createChatByUser(String nameChat, TypeChat typeChat, User user);

    List<Chat> readListChatsByUser(User user);

    List<Chat> readListChatsByChatName(String nameChat);

    Chat getChat(String nameChat, Long userId);
}
