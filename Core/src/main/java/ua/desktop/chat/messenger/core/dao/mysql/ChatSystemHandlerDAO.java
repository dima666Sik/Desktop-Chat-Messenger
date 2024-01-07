package ua.desktop.chat.messenger.core.dao.mysql;

import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface ChatSystemHandlerDAO {
    boolean isExistChatByUser(String nameChat, Long userId) throws OpenSessionException;

    boolean createChatByUser(String nameChat, TypeChat typeChat, User user, Long idUserCompanion) throws OpenSessionException;

    List<Chat> readListChatsByUser(User user) throws OpenSessionException;

    Optional<Chat> readChat(String nameChat, Long userId)throws OpenSessionException;

    List<Chat> readListChatsByChatName(String nameChat)throws OpenSessionException;

    Optional<Chat> readChatCompanion(Chat chat)throws OpenSessionException;
    List<Chat> readChatsByType(TypeChat typeChat, Long userId) throws OpenSessionException;

}
