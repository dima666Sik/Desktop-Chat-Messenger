package ua.desktop.chat.messenger.core.dao.mysql.impl;

import org.junit.jupiter.api.*;
import ua.desktop.chat.messenger.auth.dao.mysql.UserDAO;
import ua.desktop.chat.messenger.auth.dao.util.DAOFactory;
import ua.desktop.chat.messenger.core.dao.mysql.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.core.dao.mysql.util.CommonTestMethod;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.User;
import ua.desktop.chat.messenger.encryption.Encryption;
import ua.desktop.chat.messenger.exception.EncryptionException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatSystemHandlerDAOMySQLImplIT {
    private static ChatSystemHandlerDAO chatSystemHandlerDAO;
    private static UserDAO userDAO;
    private static User user1;
    private static User user2;
    private static Chat chatGroup;
    private static Chat chatPrivate1;
    private static Chat chatPrivate2;


    @BeforeAll
    static void setupUp() {
        try {
            userDAO = DAOFactory.createUserAuthDao();

            user1 = new User("devil", "devil", Encryption.encryptionSHA3256("devil"));
            user2 = new User("angel", "angel", Encryption.encryptionSHA3256("angel"));

            userDAO.createUser(user1);
            userDAO.createUser(user2);

            var userByEmailAndPassword1 = userDAO.findUserByEmailAndPassword(user1.getEmail(), user1.getPassword());
            var userByEmailAndPassword2 = userDAO.findUserByEmailAndPassword(user2.getEmail(), user2.getPassword());

            if (userByEmailAndPassword1.isEmpty() || userByEmailAndPassword2.isEmpty()) {
                throw new RuntimeException("Users was not found!");
            }

            user1 = userByEmailAndPassword1.get();
            user2 = userByEmailAndPassword2.get();

            chatGroup = new Chat(7L, "Test Termopasta Grizli", TypeChat.GROUP, user1, null);
            chatPrivate1 = new Chat(8L, user2.getUsername(), TypeChat.PRIVATE, user1, user2.getId());
            chatPrivate2 = new Chat(9L, user1.getUsername(), TypeChat.PRIVATE, user2, user1.getId());

        } catch (EncryptionException | OpenSessionException e) {
            throw new RuntimeException(e);
        }

        chatSystemHandlerDAO = ua.desktop.chat.messenger.core.dao.util.DAOFactory.createChatSystemHandlerDAO();

    }

    @Test
    @Order(1)
    void createChatByUser() {
        assertAll(() -> assertTrue(chatSystemHandlerDAO.createChatByUser(chatGroup.getNameChat(), chatGroup.getTypeChat(), chatGroup.getUser(), chatGroup.getUserCompanionId())),
                () -> assertTrue(chatSystemHandlerDAO.createChatByUser(chatPrivate1.getNameChat(), chatPrivate1.getTypeChat(), chatPrivate1.getUser(), chatPrivate1.getUserCompanionId())),
                () -> assertTrue(chatSystemHandlerDAO.createChatByUser(chatPrivate2.getNameChat(), chatPrivate2.getTypeChat(), chatPrivate2.getUser(), chatPrivate2.getUserCompanionId())));
    }

    @Test
    @Order(6)
    void readListChatsByUser() {
        Assertions.assertDoesNotThrow(() -> {
            List<Chat> chat = chatSystemHandlerDAO.readListChatsByUser(user1);
            if (chat.isEmpty()) fail();
        }, "Reading the chat wasn't successful!");
    }

    @Test
    @Order(7)
    void readListChatsByChatName() {
        Assertions.assertDoesNotThrow(() -> {
            List<Chat> chat = chatSystemHandlerDAO.readListChatsByChatName(chatGroup.getNameChat());
            if (chat.isEmpty()) fail();
        }, "Reading the chat wasn't successful!");
    }

    @Test
    @Order(3)
    void readChat() {
        Assertions.assertDoesNotThrow(() -> {
            Optional<Chat> optionalChat
                    = chatSystemHandlerDAO.findChatByChatNameAndUserId(chatGroup.getNameChat(), chatGroup.getUser()
                                                                                                         .getId());
            if (optionalChat.isEmpty()) fail();
        }, "Reading the chat wasn't successful!");
    }

    @Test
    @Order(4)
    void readChatCompanion() {
        Assertions.assertDoesNotThrow(() -> {
            Optional<Chat> optionalChat = chatSystemHandlerDAO.readChatCompanion(chatPrivate1);
            if (optionalChat.isEmpty()) fail();
        }, "Reading the chat wasn't successful!");
    }

    @Test
    @Order(5)
    void readChatsByType() {
        Assertions.assertDoesNotThrow(() -> {
            List<Chat> chat = chatSystemHandlerDAO.readChatsByType(chatGroup.getTypeChat(), user1.getId());
            if (chat.isEmpty()) fail();
        }, "Reading the chat wasn't successful!");
    }

    @AfterAll
    static void tearDown() {
        var listChatGroup = List.of(chatGroup, chatPrivate1, chatPrivate2);
        listChatGroup.forEach((chat) -> CommonTestMethod.deleteUserChat(chatSystemHandlerDAO, chat));

        var listUsers = List.of(user1, user2);
        listUsers.forEach((user) -> CommonTestMethod.deleteUser(userDAO, user));
    }
}