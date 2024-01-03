package ua.desktop.chat.messenger.auth.dao.ifaces;

import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.model.User;

import java.util.Optional;

public interface UserDAO {
    boolean createUser(final User user) throws DAOException;

    Optional<User> findUserByEmailAndPassword(final String email, final String password) throws DAOException;
}
