package ua.desktop.chat.messenger.core.dao.mysql.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ua.desktop.chat.messenger.core.dao.mysql.MessageSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.core.dao.query.hql.QueryMessageSystemHandler;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageSystemHandlerDAOMySQLImpl implements MessageSystemHandlerDAO {
    private static final Logger logger = LogManager.getLogger(MessageSystemHandlerDAOMySQLImpl.class.getName());

    @Override
    public boolean createMessageByChat(Message message) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            try {
                session.persist(message);

                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                logger.error(e);
                throw new OpenSessionException("Transaction isn't successful! Rollback data.", e);
            }

            logger.info("Create message in chat was successful!");
        }

        return true;
    }

    @Override
    public List<Message> readListMessageByChats(List<Chat> chatList) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            List<Message> chatMessages = new ArrayList<>();

            for (Chat chat : chatList) {
                Query<Message> messageQuery = session.createQuery(QueryMessageSystemHandler.READ_MESSAGES_BY_CHAT_ID, Message.class);
                messageQuery.setParameter("chatId", chat.getId());
                chatMessages.addAll(messageQuery.list());
            }

            session.getTransaction().commit();
            logger.info("Read messages from chat was successful!");

            return chatMessages;
        }
    }
}
