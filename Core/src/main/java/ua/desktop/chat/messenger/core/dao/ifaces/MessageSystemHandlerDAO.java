package ua.desktop.chat.messenger.core.dao.ifaces;

import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.model.Chat;
import ua.desktop.chat.messenger.model.Message;

import java.util.List;
import java.util.Optional;

public interface MessageSystemHandlerDAO {
    boolean createMessageByChat(Message message) throws DAOException;

    Optional<List<Message>> readListMessageByChats(List<Chat> chatList) throws DAOException;
}
