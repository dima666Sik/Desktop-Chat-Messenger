package ua.desktop.chat.messenger.dao.ifaces;

import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

import java.util.List;

public interface MessageSystemHandlerDAO {
    Message createMessageByChat(Message message) throws DAOException;

    List<Message> readListMessageByChats(List<Chat> chatList) throws DAOException;
}
