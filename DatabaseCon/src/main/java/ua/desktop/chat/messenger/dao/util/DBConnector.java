package ua.desktop.chat.messenger.dao.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

public class DBConnector {
    private final static Logger logger = LogManager.getLogger(DBConnector.class.getName());
    public static Session getSession() throws DAOException {
        Session session = null;
        try {
            Configuration configuration = new Configuration()
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Chat.class)
                    .addAnnotatedClass(Message.class);
            SessionFactory sessionFactory = configuration.buildSessionFactory();
            session = sessionFactory.openSession();
            logger.info("Get session was successful!");
        } catch (Exception e) {
            logger.error(e);
            throw new DAOException("Get session was not successful!", e);
        }
        return session;
    }
}
