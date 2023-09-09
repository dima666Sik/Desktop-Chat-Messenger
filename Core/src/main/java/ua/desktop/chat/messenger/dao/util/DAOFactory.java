package ua.desktop.chat.messenger.dao.util;

import ua.desktop.chat.messenger.dao.ifaces.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.ifaces.MessageSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.mysql.impl.ChatSystemHandlerDAOMySQLImpl;
import ua.desktop.chat.messenger.dao.mysql.impl.MessageSystemHandlerDAOMySQLImpl;

/**
 * Factory for creating data access objects (DAO).
 * Used to obtain instances of DAO implementations for specific objects.
 */
public class DAOFactory {

    public static ChatSystemHandlerDAO getChatSystemHandlerDAO() {
        return new ChatSystemHandlerDAOMySQLImpl();
    }
    public static MessageSystemHandlerDAO getMessageSystemHandlerDAO() {
        return new MessageSystemHandlerDAOMySQLImpl();
    }
}
