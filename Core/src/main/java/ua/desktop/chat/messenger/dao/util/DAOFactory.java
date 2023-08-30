package ua.desktop.chat.messenger.dao.util;

import ua.desktop.chat.messenger.dao.ifaces.ChatSystemMessageDAO;
import ua.desktop.chat.messenger.dao.ifaces.UserDAO;
import ua.desktop.chat.messenger.dao.mysql.impl.ChatSystemMessageDAOMySQLImpl;
import ua.desktop.chat.messenger.dao.mysql.impl.UserDAOMySQLImpl;

/**
 * Factory for creating data access objects (DAO).
 * Used to obtain instances of DAO implementations for specific objects.
 */
public class DAOFactory {
    public static UserDAO getUserAuthDao() {
        return new UserDAOMySQLImpl();
    }

    public static ChatSystemMessageDAO getChatSystemMessageDAO() {
        return new ChatSystemMessageDAOMySQLImpl();
    }
}
