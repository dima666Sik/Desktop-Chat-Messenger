package ua.desktop.chat.messenger.core.dao.mysql;

import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.Message;

import java.util.List;

public interface MessageSystemHandlerDAO {
    boolean createMessageByChat(Message message) throws OpenSessionException;

    List<Message> readListMessageByChats(List<Chat> chatList) throws OpenSessionException;
}
