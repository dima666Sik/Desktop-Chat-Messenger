package ua.desktop.chat.messenger.auth.dao.mysql.impl;

import org.junit.jupiter.api.*;
import ua.desktop.chat.messenger.auth.dao.mysql.UserDAO;
import ua.desktop.chat.messenger.auth.dao.util.DAOFactory;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.model.User;
import ua.desktop.chat.messenger.encryption.Encryption;
import ua.desktop.chat.messenger.exception.EncryptionException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOMySQLImplTest {
    private static User user;
    private static UserDAO userDAO;

    @BeforeAll
    static void setup() {
        try {
            userDAO = DAOFactory.createUserAuthDao();
            user = new User("devil", "devil", "devil");
            user.setPassword(Encryption.encryptionSHA3256(user.getPassword()));
        } catch (EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void createUser() {
        Assertions.assertDoesNotThrow(() -> Assertions.assertTrue(userDAO.createUser(user)), "Creating the user wasn't successful!");
    }

    @Test
    @Order(2)
    void findUserByEmailAndPassword() {
        Assertions.assertDoesNotThrow(() -> {
            var user = userDAO.findUserByEmailAndPassword(UserDAOMySQLImplTest.user.getEmail(), UserDAOMySQLImplTest.user.getPassword());
            if (user.isEmpty()) fail();
            UserDAOMySQLImplTest.user.setPassword(user.get()
                                                      .getPassword());
            Assertions.assertEquals(UserDAOMySQLImplTest.user, user.get());
        }, "Reading the user wasn't successful!");
    }

    @AfterAll
    static void tearDown() throws OpenSessionException {
        userDAO
                .findUserByEmailAndPassword(user.getEmail(), user.getPassword())
                .ifPresent((resSearch) -> {
                    try (var session = DBConnector.getSession()) {
                        try {
                            session.beginTransaction();
                            session
                                    .createQuery("DELETE FROM User WHERE username=:username and email=:email and password=:password")
                                    .setParameter("username", user.getUsername())
                                    .setParameter("email", user.getEmail())
                                    .setParameter("password", user.getPassword())
                                    .executeUpdate();

                            session.getTransaction()
                                   .commit();
                        } catch (Exception e) {
                            session.getTransaction()
                                   .rollback();
                            throw e;
                        }
                    } catch (OpenSessionException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}