package ua.desktop.chat.messenger.core.dao.mysql.impl;

import org.hibernate.Session;
import org.junit.jupiter.api.*;
import ua.desktop.chat.messenger.auth.dao.mysql.UserDAO;
import ua.desktop.chat.messenger.core.dao.mysql.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.core.dao.mysql.MessageSystemHandlerDAO;
import ua.desktop.chat.messenger.core.dao.mysql.util.CommonTestMethod;
import ua.desktop.chat.messenger.core.dao.util.DAOFactory;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.Message;
import ua.desktop.chat.messenger.domain.model.User;
import ua.desktop.chat.messenger.encryption.Encryption;
import ua.desktop.chat.messenger.exception.EncryptionException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageSystemHandlerDAOMySQLIT {
    private static UserDAO userDAO;
    private static MessageSystemHandlerDAO messageSystemHandlerDAO;
    private static ChatSystemHandlerDAO chatSystemHandlerDAO;
    private static User user;
    private static Chat chatGroup;
    private static Message message;

    @BeforeAll
    static void setupUp() {
        try {
            userDAO = ua.desktop.chat.messenger.auth.dao.util.DAOFactory.createUserAuthDao();
            chatSystemHandlerDAO = DAOFactory.createChatSystemHandlerDAO();
            messageSystemHandlerDAO = DAOFactory.createMessageSystemHandlerDAO();

            user = new User("devil", "devil", Encryption.encryptionSHA3256("devil"));

            userDAO.createUser(user);

            var userByEmailAndPassword = userDAO.findUserByEmailAndPassword(user.getEmail(), user.getPassword());

            if (userByEmailAndPassword.isEmpty()) {
                throw new RuntimeException("Users was not found!");
            }

            user = userByEmailAndPassword.get();

            chatGroup = new Chat(7L, "Test Termopasta Grizli", TypeChat.GROUP, user, null);
            chatSystemHandlerDAO.createChatByUser(chatGroup.getNameChat(), chatGroup.getTypeChat(), chatGroup.getUser(), chatGroup.getUserCompanionId());

            var chatByChatNameAndUserId = chatSystemHandlerDAO
                    .findChatByChatNameAndUserId(chatGroup.getNameChat(), chatGroup.getUser()
                                                                                   .getId());

            if (chatByChatNameAndUserId.isEmpty()) {
                throw new RuntimeException("Chat was not found!");
            }

            chatGroup = chatByChatNameAndUserId.get();

            message = new Message("Heeelp mee this is test msg...", LocalDateTime.now(), chatGroup);

        } catch (EncryptionException | OpenSessionException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @Order(1)
    void createMessageByChat() {
        Assertions.assertDoesNotThrow(() -> messageSystemHandlerDAO.createMessageByChat(message));
    }

    @Test
    @Order(2)
    void readListMessageByChats() throws OpenSessionException {
        List<Message> messageList = messageSystemHandlerDAO.readListMessageByChats(List.of(chatGroup));
        if (messageList.isEmpty()) fail();
        Assertions.assertNotNull(messageList.toArray());
    }

    @AfterAll
    static void tearDown() throws OpenSessionException {
        var listChatGroup = List.of(MessageSystemHandlerDAOMySQLIT.chatGroup);
        var listUsers = List.of(user);

        messageSystemHandlerDAO.readListMessageByChats(listChatGroup)
                               .forEach(CommonTestMethod::deleteUserChatMessage);

        listChatGroup.forEach((chat) -> CommonTestMethod.deleteUserChat(chatSystemHandlerDAO, chat));

        listUsers.forEach((user) -> CommonTestMethod.deleteUser(userDAO, user));
    }

}