package ua.desktop.chat.messenger.dao.mysql.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.dao.ifaces.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.query.hql.QueryChatSystemHandler;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;
import java.util.Optional;

public class ChatSystemHandlerDAOMySQLImpl implements ChatSystemHandlerDAO {
    private final static Logger logger = LogManager.getLogger(ChatSystemHandlerDAOMySQLImpl.class.getName());

    @Override
    public boolean isExistChatByUser(String nameChat, Long userId) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Optional<Chat> foundChat = findChatByChatNameAndUserId(nameChat, userId);

            if (foundChat.isPresent()) {
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
    public boolean createChatByUser(String nameChat, TypeChat typeChat, User user, Long idUserCompanion) throws DAOException {
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

            return true;
        }
    }

    @Override
    public Optional<List<Chat>> readListChatsByUser(User user) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.readChatsByUserId(), Chat.class);
            query.setParameter("userId", user.getId());

            Optional<List<Chat>> chatList = Optional.ofNullable(query.list());

            session.getTransaction().commit();
            logger.info("Read chats was successful!");

            return chatList;
        }
    }

    @Override
    public Optional<Chat> readChat(String nameChat, Long userId) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Optional<Chat> chatORM = findChatByChatNameAndUserId(nameChat, userId);
            logger.info("Read chats was successful!");

            session.getTransaction().commit();

            return chatORM;
        }

    }

    @Override
    public Optional<List<Chat>> readChatsByType(TypeChat typeChat, Long userId) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.findChatsByTypeChatAndUserId(), Chat.class);
            query.setParameter("typeChat", typeChat);
            query.setParameter("userId", userId);

            Optional<List<Chat>> chatList = Optional.ofNullable(query.list());

            logger.info("Read chats was successful!");

            session.getTransaction().commit();

            return chatList;
        }

    }

    @Override
    public Optional<List<Chat>> readListChatsByChatName(String nameChat) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> chatQuery = session.createQuery(QueryChatSystemHandler.findChatsByName(), Chat.class);
            chatQuery.setParameter("nameChat", nameChat);
            Optional<List<Chat>> chatList = Optional.ofNullable(chatQuery.list());

            session.getTransaction().commit();
            return chatList;
        }
    }

    @Override
    public Optional<Chat> readChatCompanion(Chat chat) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.findChatCompanionByUserCompanionIdAndUserId(), Chat.class);
            query.setParameter("userCompanionId", chat.getUser().getId());
            query.setParameter("userId", chat.getUserCompanionId());

            Optional<Chat> chatORM = Optional.ofNullable(query.uniqueResult());

            logger.info("Read chats was successful!");
            session.getTransaction().commit();

            return chatORM;
        }
    }

    private Optional<Chat> findChatByChatNameAndUserId(String nameChat, Long userId) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemHandler.findChatByChatNameAndUserId(), Chat.class);
            query.setParameter("nameChat", nameChat);
            query.setParameter("userId", userId);

            Optional<Chat> chatORM = Optional.ofNullable(query.uniqueResult());
            session.getTransaction().commit();

            return chatORM;
        }
    }
}
