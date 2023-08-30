package ua.desktop.chat.messenger.dao.ifaces;

import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.models.User;

public interface UserDAO {
    boolean createUser(final User user) throws DAOException;

    User findUserByEmailAndPassword(final String email, final String password) throws DAOException;
}
