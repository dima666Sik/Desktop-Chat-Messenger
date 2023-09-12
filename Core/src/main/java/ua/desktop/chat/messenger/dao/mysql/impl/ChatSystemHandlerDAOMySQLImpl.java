package ua.desktop.chat.messenger.dao.mysql.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.dao.ifaces.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.query.hql.QueryChatSystemHandler;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;

public class ChatSystemHandlerDAOMySQLImpl implements ChatSystemHandlerDAO {
    private final static Logger logger = LogManager.getLogger(ChatSystemHandlerDAOMySQLImpl.class.getName());

    @Override
    public boolean isExistChatByUser(String nameChat, Long userId) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Chat foundChat = findChatByChatNameAndUserId(nameChat, userId);

            if (foundChat != null) {
                logger.info("Chat with this name `"
                        .concat(nameChat)
                        .concat("` was existed!"));
                return true;
            }
            logger.info("Chat with this name `"
                    .concat(nameChat)
                    .concat("` was not existed!"));
            session.getTransaction().commit();

            return false;
        }
    }

    @Override
    public Chat createChatByUser(String nameChat, TypeChat typeChat, User user, Long idUserCompanion) throws DAOException {
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
                throw new DAOException("Transaction isn't successful! Rollback data.", e);
            }

            logger.info("Create chat was successful!");

            return chat;
        }
    }

    @Override
    public List<Chat> readListChatsByUser(User user) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.readChatsByUserId(), Chat.class);
            query.setParameter("userId", user.getId());

            List<Chat> chatList = query.list();

            session.getTransaction().commit();
            logger.info("Read chats was successful!");

            return chatList;
        }
    }

    @Override
    public Chat getChat(String nameChat, Long userId) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Chat chatORM = findChatByChatNameAndUserId(nameChat, userId);
            logger.info("Read chats was successful!");

            session.getTransaction().commit();

            return chatORM;
        }

    }

    @Override
    public List<Chat> readListChatsByChatName(String nameChat) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> chatQuery = session.createQuery(QueryChatSystemHandler.findChatsByName(), Chat.class);
            chatQuery.setParameter("nameChat", nameChat);
            List<Chat> chatList = chatQuery.list();

            session.getTransaction().commit();
            return chatList;
        }
    }

    @Override
    public Chat getChatCompanion(Chat chat) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.findChatCompanionByUserCompanionIdAndUserId(), Chat.class);
            query.setParameter("userCompanionId", chat.getUser().getId());
            query.setParameter("userId", chat.getUserCompanionId());

            Chat chatORM = query.uniqueResult();

            logger.info("Read chats was successful!");
            session.getTransaction().commit();

            return chatORM;
        }
    }

    private Chat findChatByChatNameAndUserId(String nameChat, Long userId) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.findChatByChatNameAndUserId(), Chat.class);
            query.setParameter("nameChat", nameChat);
            query.setParameter("userId", userId);

            Chat chatORM = query.uniqueResult();
            session.getTransaction().commit();
            return chatORM;
        }
    }
}
