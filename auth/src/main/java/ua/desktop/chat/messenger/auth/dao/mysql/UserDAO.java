package ua.desktop.chat.messenger.auth.dao.mysql;

import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.model.User;

import java.util.Optional;

public interface UserDAO {
    boolean createUser(final User user) throws OpenSessionException;
    boolean isExistUserWithEmailAndUserName( final User user) throws OpenSessionException;
    Optional<User> findUserByEmailAndPassword(final String email, final String password) throws OpenSessionException;
}
