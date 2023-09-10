package ua.desktop.chat.messenger.dao.ifaces;

import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;

public interface ChatSystemHandlerDAO {
    boolean isExistChatByUser(String nameChat, Long userId) throws DAOException;

    Chat createChatByUser(String nameChat, TypeChat typeChat, User user, Long idUserCompanion) throws DAOException;

    List<Chat> readListChatsByUser(User user) throws DAOException;

    Chat getChat(String nameChat, Long userId)throws DAOException;

    List<Chat> readListChatsByChatName(String nameChat)throws DAOException;

    Chat getChatCompanion(Chat chat)throws DAOException;
}
