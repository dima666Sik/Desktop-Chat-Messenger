package ua.desktop.chat.messenger.auth.dao.util;

import ua.desktop.chat.messenger.auth.dao.mysql.UserDAO;
import ua.desktop.chat.messenger.auth.dao.mysql.impl.UserDAOMySQLImpl;

/**
 * Factory for creating data access objects (DAO).
 * Used to obtain instances of DAO implementations for specific objects.
 */
public class DAOFactory {
    private DAOFactory(){}
    public static UserDAO createUserAuthDao() {
        return new UserDAOMySQLImpl();
    }
}
