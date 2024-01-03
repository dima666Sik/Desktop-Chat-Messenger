package ua.desktop.chat.messenger.dao.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.model.Chat;
import ua.desktop.chat.messenger.model.Message;
import ua.desktop.chat.messenger.model.User;

public class DBConnector {
    private final static Logger logger = LogManager.getLogger(DBConnector.class.getName());
    private final static SessionFactory sessionFactory;

    static {
        Configuration configuration = new Configuration()
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Chat.class)
                .addAnnotatedClass(Message.class);

        sessionFactory = configuration.buildSessionFactory();
    }

    public static Session getSession() throws DAOException {
        try {
            logger.info("Get session was successful!");
            return sessionFactory.openSession();
        } catch (Exception e) {
            logger.error(e);
            throw new DAOException("Get session was not successful!", e);
        }
    }

    public static void closeSessionFactory() {
        sessionFactory.close();
    }
}
