package ua.desktop.chat.messenger.dao.ifaces;

import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;

public interface ChatSystemMessageDAO {
    boolean isExistChatByUser(String nameChat, User user) throws DAOException;

    Chat createChatByUser(String nameChat, TypeChat typeChat, User user) throws DAOException;

    List<Chat> readListChatsByUser(User user) throws DAOException;
}
