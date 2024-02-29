package ua.desktop.chat.messenger.dao.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.Message;
import ua.desktop.chat.messenger.domain.model.User;

public class DBConnector {
    private static final Logger logger = LogManager.getLogger(DBConnector.class.getName());
    private static final SessionFactory SESSION_FACTORY;

    static {
        Configuration configuration = new Configuration()
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Chat.class)
                .addAnnotatedClass(Message.class);

        SESSION_FACTORY = configuration.buildSessionFactory();
    }

    private DBConnector(){}

    public static Session getSession() throws OpenSessionException {
        try {
            logger.info("Get session was successful!");
            return SESSION_FACTORY.openSession();
        } catch (Exception e) {
            logger.error(e);
            throw new OpenSessionException("Get session was not successful!", e);
        }
    }

    public static void closeSessionFactory() {
        SESSION_FACTORY.close();
    }
}
