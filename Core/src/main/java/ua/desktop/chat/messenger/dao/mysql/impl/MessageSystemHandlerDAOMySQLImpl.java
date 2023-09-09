package ua.desktop.chat.messenger.dao.mysql.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.dao.ifaces.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.ifaces.MessageSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.query.hql.QueryChatSystemHandler;
import ua.desktop.chat.messenger.dao.query.hql.QueryMessageSystemHandler;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

import java.util.ArrayList;
import java.util.List;

public class MessageSystemHandlerDAOMySQLImpl implements MessageSystemHandlerDAO {
    private final static Logger logger = LogManager.getLogger(MessageSystemHandlerDAOMySQLImpl.class.getName());

    @Override
    public Message createMessageByChat(Message message) throws DAOException {

        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            session.persist(message);

            session.getTransaction().commit();
            logger.info("Create message in chat was successful!");
        }

        return message;
    }

    @Override
    public List<Message> readListMessageByChats(List<Chat> chatList) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            List<Message> chatMessages = null;

            // Для каждого чата выполняем запрос и добавляем результаты в список
            for (Chat chat : chatList) {
                Query<Message> messageQuery = session.createQuery(QueryMessageSystemHandler.readMessagesByChatId(), Message.class);
                messageQuery.setParameter("chatId", chat.getId());
                chatMessages = messageQuery.list();
            }

            session.getTransaction().commit();
            logger.info("Read messages from chat was successful!");

            return chatMessages;
        }
    }
}
