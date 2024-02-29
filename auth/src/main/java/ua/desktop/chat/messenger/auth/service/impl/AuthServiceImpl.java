package ua.desktop.chat.messenger.auth.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.auth.dao.mysql.UserDAO;
import ua.desktop.chat.messenger.auth.dao.util.DAOFactory;
import ua.desktop.chat.messenger.auth.service.AuthService;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.encryption.Encryption;
import ua.desktop.chat.messenger.exception.EncryptionException;
import ua.desktop.chat.messenger.domain.model.User;
import ua.desktop.chat.messenger.domain.util.Mapper;

import java.util.Optional;

/**
 * Implementation of {@link AuthService} interface for user authentication and registration.
 */
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class.getName());

    private final UserDAO userDAO;

    public AuthServiceImpl() {
        userDAO = DAOFactory.createUserAuthDao();
        logger.info("Getting user auth instance!");
    }

    /**
     * Implementation of the new user registration method.
     *
     * @param userName username
     * @param email    the user's email
     * @param password user password (array of characters)
     * @return true if registration was successful, otherwise false
     */
    @Override
    public boolean registration(final String userName, final String email, final String password) throws OpenSessionException {
        try {
            User user = new User(userName, email, Encryption.encryptionSHA3256(password));

            if (userDAO.isExistUserWithEmailAndUserName(user)) {
                logger.warn("Such user with email {} is defined, please change email...", user.getEmail());
                return false;
            }

            return userDAO.createUser(user);
        } catch (EncryptionException e) {
            logger.error("Cryptographic algorithm isn't available!", e);
        }
        return false;
    }

    /**
     * Implementation of the user authorization method.
     *
     * @param email    the user's email
     * @param password user password (array of characters)
     * @return a user object if authentication succeeds, or null if the user is not found or authentication fails
     */
    @Override
    public Optional<UserDTO> authorization(final String email, final String password) throws OpenSessionException {
        Optional<UserDTO> userDTO = Optional.empty();
        try {
            Optional<User> userORM = userDAO.findUserByEmailAndPassword(email, Encryption.encryptionSHA3256(password));
            if (userORM.isEmpty()) {
                logger.warn("User is empty! Finding user wasn't successful!");
                return userDTO;
            }
            userDTO = Optional.of(Mapper.convertUserIntoUserDTO(userORM.get()));
        } catch (EncryptionException e) {
            logger.error("Cryptographic algorithm isn't available!", e);
        }

        return userDTO;
    }


}
