package ua.desktop.chat.messenger.auth.domain.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.auth.dao.ifaces.UserDAO;
import ua.desktop.chat.messenger.auth.dao.util.DAOFactory;
import ua.desktop.chat.messenger.auth.dao.util.Encryption;
import ua.desktop.chat.messenger.auth.domain.exceptions.DomainException;
import ua.desktop.chat.messenger.auth.domain.ifaces.AuthService;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.util.Converter;

import java.util.Optional;

/**
 * Implementation of {@link AuthService} interface for user authentication and registration.
 */
public class AuthServiceImpl implements AuthService {
    private final static Logger logger = LogManager.getLogger(AuthServiceImpl.class.getName());

    private final UserDAO userDAO;

    public AuthServiceImpl() {
        userDAO = DAOFactory.createUserAuthDao();
        logger.info("Getting user auth instance!");
    }

    /**
     * Implementation of the new user registration method.
     * @param userName username
     * @param email the user's email
     * @param password user password (array of characters)
     * @return true if registration was successful, otherwise false
     */
    @Override
    public boolean registration(final String userName, final String email, final String password) {

        try {
            User user = new User(userName,  email, Encryption.encryptionSHA3256(password));
            try {
                return userDAO.createUser(user);
            } catch (DAOException e) {
                logger.error("Cannot create user!", e);
                return false;
            }
        } catch (DomainException e) {
            logger.error("Cryptographic algorithm isn't available!", e);
            return false;
        }
    }

    /**
     * Implementation of the user authorization method.
     * @param email the user's email
     * @param password user password (array of characters)
     * @return a user object if authentication succeeds, or null if the user is not found or authentication fails
     */
    @Override
    public Optional<UserDTO> authorization(final String email, final String password) {
        Optional<UserDTO> userDTO = Optional.empty();
        try {
            Optional<User> userORM = userDAO.findUserByEmailAndPassword(email, Encryption.encryptionSHA3256(password));
            if (userORM.isEmpty()) {
                logger.warn("User is empty! Finding user wasn't successful!");
                return userDTO;
            }
            userDTO = Optional.of(Converter.convertUserIntoUserDTO(userORM.get()));
        } catch (DAOException e) {
            logger.error("Cannot create user!", e);
        } catch (DomainException e) {
            logger.error("Cryptographic algorithm isn't available!", e);
        }

        return userDTO;
    }


}
