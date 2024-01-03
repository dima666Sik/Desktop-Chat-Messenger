package ua.desktop.chat.messenger.auth.dao.mysql.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.auth.dao.ifaces.UserDAO;
import ua.desktop.chat.messenger.auth.dao.query.hql.QueryUser;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.model.User;

import java.util.Optional;

public class UserDAOMySQLImpl implements UserDAO {
    private final static Logger logger = LogManager.getLogger(UserDAOMySQLImpl.class.getName());

    @Override
    public boolean createUser(final User user) throws DAOException {

        if (isExistUserWithEmailAndUserName(user)) {
            logger.warn("Such user with email `"
                    .concat(user.getEmail())
                    .concat("` is defined, please change email..."));
            return false;
        }

        if (userIsExist(user)) {
            logger.warn("Such user is defined, please change login...");
            return false;
        }

        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();
            try {
                session.persist(user);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                logger.error(e);
                throw new DAOException("Transaction isn't successful! Rollback data.", e);
            }
            logger.info("Create user was successful!");
        }

        return true;
    }

    private boolean isExistUserWithEmailAndUserName(User user) throws DAOException {

        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<User> query = session.createQuery(QueryUser.findUserByEmailORUserName(), User.class);
            query.setParameter("email", user.getEmail());
            query.setParameter("username", user.getUsername());

            User foundUser = query.uniqueResult();

            session.getTransaction().commit();

            if (foundUser != null) {
                logger.info("User with this email `"
                        .concat(user.getEmail())
                        .concat("` or username `")
                        .concat(user.getUsername())
                        .concat("`was existed!"));
                return true;
            }
            logger.info("User with this email `"
                    .concat(user.getEmail())
                    .concat("` or username `")
                    .concat(user.getUsername())
                    .concat("` was not existed!"));
            return false;
        }
    }

    @Override
    public Optional<User> findUserByEmailAndPassword(String email, String password) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<User> query = session.createQuery(QueryUser.findUserByEmailAndPassword(), User.class);
            query.setParameter("email", email);
            query.setParameter("password", password);

            User foundUser = query.uniqueResult();
            session.getTransaction().commit();

            if (foundUser != null) {
                logger.info("User was found!");
                return Optional.of(foundUser);
            }

            logger.info("User was not found!");
            return Optional.empty();
        }
    }

    private boolean userIsExist(User user) throws DAOException {

        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<User> query = session.createQuery(QueryUser.findUserByEmailAndPassword(), User.class);
            query.setParameter("email", user.getEmail());
            query.setParameter("password", user.getPassword());

            User foundUser = query.uniqueResult();

            session.getTransaction().commit();

            if (foundUser != null) {
                logger.info("User was existed!");
                return true;
            }
            logger.info("User was not existed!");
            return false;
        }
    }

}
