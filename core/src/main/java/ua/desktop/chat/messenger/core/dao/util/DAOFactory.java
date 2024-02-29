package ua.desktop.chat.messenger.core.dao.util;

import ua.desktop.chat.messenger.core.dao.mysql.MessageSystemHandlerDAO;
import ua.desktop.chat.messenger.core.dao.mysql.impl.ChatSystemHandlerDAOMySQLImpl;
import ua.desktop.chat.messenger.core.dao.mysql.impl.MessageSystemHandlerDAOMySQLImpl;
import ua.desktop.chat.messenger.core.dao.mysql.ChatSystemHandlerDAO;

/**
 * Factory for creating data access objects (DAO).
 * Used to obtain instances of DAO implementations for specific objects.
 */
public class DAOFactory {
    private DAOFactory(){}

    public static ChatSystemHandlerDAO createChatSystemHandlerDAO() {
        return new ChatSystemHandlerDAOMySQLImpl();
    }
    public static MessageSystemHandlerDAO createMessageSystemHandlerDAO() {
        return new MessageSystemHandlerDAOMySQLImpl();
    }
}
