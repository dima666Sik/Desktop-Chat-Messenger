package ua.desktop.chat.messenger.dao.util;

import ua.desktop.chat.messenger.dao.ifaces.ChatSystemMessageDAO;
import ua.desktop.chat.messenger.dao.mysql.impl.ChatSystemMessageDAOMySQLImpl;

/**
 * Factory for creating data access objects (DAO).
 * Used to obtain instances of DAO implementations for specific objects.
 */
public class DAOFactory {

    public static ChatSystemMessageDAO getChatSystemMessageDAO() {
        return new ChatSystemMessageDAOMySQLImpl();
    }
}
