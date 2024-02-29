package ua.desktop.chat.messenger.core.dao.mysql.util;

import org.hibernate.Session;
import ua.desktop.chat.messenger.auth.dao.mysql.UserDAO;
import ua.desktop.chat.messenger.core.dao.mysql.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.Message;
import ua.desktop.chat.messenger.domain.model.User;

public final class CommonTestMethod {
    private CommonTestMethod() {
    }

    public static void deleteUserChat(ChatSystemHandlerDAO chatSystemHandlerDAO, Chat chat) {
        try {
            chatSystemHandlerDAO.findChatByChatNameAndUserId(chat.getNameChat(), chat.getUser()
                                                                                     .getId())
                                .ifPresent((resSearch) -> {
                                    try (Session session = DBConnector.getSession()) {
                                        try {
                                            session.beginTransaction();
                                            session.createQuery("DELETE FROM Chat WHERE nameChat=:nameChat and typeChat=:typeChat and user.id=:userId")
                                                   .setParameter("nameChat", chat.getNameChat())
                                                   .setParameter("typeChat", chat.getTypeChat())
                                                   .setParameter("userId", chat.getUser()
                                                                               .getId())
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
        } catch (OpenSessionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteUser(UserDAO userDAO, User user) {
        try {
            userDAO.findUserByEmailAndPassword(user.getEmail(), user.getPassword())
                   .ifPresent((resSearch) -> {
                       try (Session session = DBConnector.getSession()) {
                           try {
                               session.beginTransaction();
                               session.createQuery("DELETE FROM User WHERE username=:username and email=:email and password=:password")
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
        } catch (OpenSessionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteUserChatMessage(Message message) {
        try (Session session = DBConnector.getSession()) {
            try {
                session.beginTransaction();
                session.createQuery("DELETE FROM Message WHERE chat.id=:id")
                       .setParameter("id", message.getChat()
                                                     .getId())
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
    }
}
