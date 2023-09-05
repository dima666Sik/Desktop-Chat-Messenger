package ua.desktop.chat.messenger.auth.dao.util;

import ua.desktop.chat.messenger.auth.dao.ifaces.UserDAO;
import ua.desktop.chat.messenger.auth.dao.mysql.impl.UserDAOMySQLImpl;

/**
 * Factory for creating data access objects (DAO).
 * Used to obtain instances of DAO implementations for specific objects.
 */
public class DAOFactory {
    public static UserDAO getUserAuthDao() {
        return new UserDAOMySQLImpl();
    }
}
