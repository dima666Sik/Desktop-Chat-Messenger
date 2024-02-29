package ua.desktop.chat.messenger.auth.dao.mysql.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.auth.dao.mysql.UserDAO;
import ua.desktop.chat.messenger.auth.dao.query.hql.QueryUser;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.domain.model.User;

import java.util.Optional;

public class UserDAOMySQLImpl implements UserDAO {
    private static final Logger logger = LogManager.getLogger(UserDAOMySQLImpl.class.getName());

    @Override
    public boolean createUser(final User user) throws OpenSessionException {
        try (var session = DBConnector.getSession()) {
            try {
                session.beginTransaction();
                session.persist(user);
                session.getTransaction()
                       .commit();
                logger.info("Create user was successful!");
            } catch (Exception e) {
                session.getTransaction()
                       .rollback();
                throw e;
            }
        }
        return true;
    }

    public boolean isExistUserWithEmailAndUserName(final User user) throws OpenSessionException {

        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<User> query = session.createQuery(QueryUser.FIND_USER_BY_EMAIL_OR_USERNAME, User.class);
            query.setParameter("email", user.getEmail());
            query.setParameter("username", user.getUsername());

            User foundUser = query.uniqueResult();

            session.getTransaction()
                   .commit();

            if (foundUser != null) {
                logger.info("User with this email {} or username {} was existed!", user.getEmail(), user.getUsername());
                return true;
            }

            logger.info("User with this email {} or username {} was existed!", user.getEmail(), user.getUsername());
            return false;
        }
    }

    @Override
    public Optional<User> findUserByEmailAndPassword(String email, String password) throws OpenSessionException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<User> query = session.createQuery(QueryUser.FIND_USER_BY_EMAIL_AND_PASSWORD, User.class);
            query.setParameter("email", email);
            query.setParameter("password", password);

            User foundUser = query.uniqueResult();
            session.getTransaction()
                   .commit();

            if (foundUser != null) {
                logger.info("User was found!");
                return Optional.of(foundUser);
            }

            logger.info("User was not found!");
            return Optional.empty();
        }
    }

}
