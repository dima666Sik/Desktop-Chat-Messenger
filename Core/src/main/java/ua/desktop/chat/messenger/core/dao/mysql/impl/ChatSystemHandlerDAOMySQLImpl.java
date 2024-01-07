package ua.desktop.chat.messenger.core.dao.mysql.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.core.dao.mysql.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.core.dao.query.hql.QueryChatSystemHandler;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.User;

import java.util.List;
import java.util.Optional;

public class ChatSystemHandlerDAOMySQLImpl implements ChatSystemHandlerDAO {
    private static final Logger logger = LogManager.getLogger(ChatSystemHandlerDAOMySQLImpl.class.getName());

    @Override
    public boolean isExistChatByUser(String nameChat, Long userId) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Optional<Chat> foundChat = findChatByChatNameAndUserId(nameChat, userId);

            if (foundChat.isPresent()) {
                logger.info("Chat with this name `{}` was existed!", nameChat);
                return true;
            }

            logger.info("Chat with this name `{}` was not existed!", nameChat);
            session.getTransaction().commit();

            return false;
        }
    }

    @Override
    public boolean createChatByUser(String nameChat, TypeChat typeChat, User user, Long idUserCompanion) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {

            session.beginTransaction();

            User userORM = session.get(User.class, user.getId()); // Replace userId with the actual user ID
            Chat chat = new Chat(nameChat, typeChat, userORM, idUserCompanion);

            try {
                session.persist(chat);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                logger.error(e);
                throw new OpenSessionException("Transaction isn't successful! Rollback data.", e);
            }

            logger.info("Create chat was successful!");

            return true;
        }
    }

    @Override
    public List<Chat> readListChatsByUser(User user) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.READ_CHATS_BY_USER_ID, Chat.class);
            query.setParameter("userId", user.getId());

            List<Chat> chatList = query.list();

            session.getTransaction().commit();
            logger.info("Read chats was successful!");

            return chatList;
        }
    }

    @Override
    public Optional<Chat> readChat(String nameChat, Long userId) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Optional<Chat> chatORM = findChatByChatNameAndUserId(nameChat, userId);
            logger.info("Read chats was successful!");

            session.getTransaction().commit();

            return chatORM;
        }

    }

    @Override
    public List<Chat> readChatsByType(TypeChat typeChat, Long userId) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.FIND_CHATS_BY_TYPE_CHAT_AND_USER_ID, Chat.class);
            query.setParameter("typeChat", typeChat);
            query.setParameter("userId", userId);

            List<Chat> chatList = query.list();

            logger.info("Read chats was successful!");

            session.getTransaction().commit();

            return chatList;
        }

    }

    @Override
    public List<Chat> readListChatsByChatName(String nameChat) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> chatQuery = session.createQuery(QueryChatSystemHandler.FIND_CHATS_BY_NAME, Chat.class);
            chatQuery.setParameter("nameChat", nameChat);
            List<Chat> chatList = chatQuery.list();

            session.getTransaction().commit();
            return chatList;
        }
    }

    @Override
    public Optional<Chat> readChatCompanion(Chat chat) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.FIND_CHAT_COMPANION_BY_USER_COMPANION_ID_AND_USER_ID, Chat.class);
            query.setParameter("userCompanionId", chat.getUser().getId());
            query.setParameter("userId", chat.getUserCompanionId());

            Optional<Chat> chatORM = Optional.ofNullable(query.uniqueResult());

            logger.info("Read chats was successful!");
            session.getTransaction().commit();

            return chatORM;
        }
    }

    private Optional<Chat> findChatByChatNameAndUserId(String nameChat, Long userId) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.FIND_CHAT_BY_CHAT_NAME_AND_USER_ID, Chat.class);
            query.setParameter("nameChat", nameChat);
            query.setParameter("userId", userId);

            Optional<Chat> chatORM = Optional.ofNullable(query.uniqueResult());
            session.getTransaction().commit();

            return chatORM;
        }
    }
}
