package ua.desktop.chat.messenger.dao.ifaces;

import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;
import java.util.Optional;

public interface ChatSystemHandlerDAO {
    boolean isExistChatByUser(String nameChat, Long userId) throws DAOException;

    Chat createChatByUser(String nameChat, TypeChat typeChat, User user, Long idUserCompanion) throws DAOException;

    Optional<List<Chat>> readListChatsByUser(User user) throws DAOException;

    Optional<Chat> readChat(String nameChat, Long userId)throws DAOException;

    Optional<List<Chat>> readListChatsByChatName(String nameChat)throws DAOException;

    Optional<Chat> readChatCompanion(Chat chat)throws DAOException;
    Optional<List<Chat>> readChatsByType(TypeChat typeChat, Long userId) throws DAOException;

}
