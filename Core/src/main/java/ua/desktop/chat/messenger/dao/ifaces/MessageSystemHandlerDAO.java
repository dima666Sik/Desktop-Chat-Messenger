package ua.desktop.chat.messenger.dao.ifaces;

import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

import java.util.List;
import java.util.Optional;

public interface MessageSystemHandlerDAO {
    void createMessageByChat(Message message) throws DAOException;

    Optional<List<Message>> readListMessageByChats(List<Chat> chatList) throws DAOException;
}
