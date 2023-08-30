package ua.desktop.chat.messenger.dao.mysql.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.dao.ifaces.ChatSystemMessageDAO;
import ua.desktop.chat.messenger.dao.query.hql.QueryChatSystemMessage;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;

public class ChatSystemMessageDAOMySQLImpl implements ChatSystemMessageDAO {
    private final static Logger logger = LogManager.getLogger(ChatSystemMessageDAOMySQLImpl.class.getName());

    @Override
    public boolean isExistChatByUser(String nameChat, User user) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemMessage.findChatByChatNameAndUserId(), Chat.class);
            query.setParameter("nameChat", nameChat);
            query.setParameter("userId", user.getId());

            Chat foundChat = query.uniqueResult();

            session.getTransaction().commit();

            if (foundChat != null) {
                logger.info("Chat with this name `"
                        .concat(nameChat)
                        .concat("` was existed!"));
                return true;
            }
            logger.info("Chat with this name `"
                    .concat(nameChat)
                    .concat("` was not existed!"));
            return false;
        }
    }

    @Override
    public Chat createChatByUser(String nameChat, TypeChat typeChat, User user) throws DAOException {
        Chat chat = new Chat(nameChat, typeChat, user);
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();
            session.persist(chat);
            session.getTransaction().commit();
            logger.info("Create chat was successful!");
        }

        return chat;
    }

    @Override
    public List<Chat> readListChatsByUser(User user) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<Chat> query = session.createQuery(QueryChatSystemMessage.readChatsByUserId(), Chat.class);
            query.setParameter("userId", user.getId());

            List<Chat> chatList = query.list();

            session.getTransaction().commit();
            logger.info("Read chats was successful!");

            return chatList;
        }
    }
}
