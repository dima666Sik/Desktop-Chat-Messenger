package ua.desktop.chat.messenger.auth.dao.mysql.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ua.desktop.chat.messenger.auth.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.auth.dao.ifaces.UserDAO;
import ua.desktop.chat.messenger.auth.dao.query.hql.QueryUser;
import ua.desktop.chat.messenger.auth.dao.util.DBConnector;
import ua.desktop.chat.messenger.models.User;

public class UserDAOMySQLImpl implements UserDAO {
    private final static Logger logger = LogManager.getLogger(UserDAOMySQLImpl.class.getName());

    @Override
    public boolean createUser(final User user) throws DAOException {

        if (emailUserIsExist(user)) {
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
            session.persist(user);
            session.getTransaction().commit();
            logger.info("Create user was successful!");
        }

        return true;
    }

    private boolean emailUserIsExist(User user) throws DAOException {

        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<User> query = session.createQuery(QueryUser.findUserByEmail(), User.class);
            query.setParameter("email", user.getEmail());

            User foundUser = query.uniqueResult();

            session.getTransaction().commit();

            if (foundUser != null) {
                logger.info("User with this email `"
                        .concat(user.getEmail())
                        .concat("` was existed!"));
                return true;
            }
            logger.info("User with this email `"
                    .concat(user.getEmail())
                    .concat("` was not existed!"));
            return false;
        }
    }

    @Override
    public User findUserByEmailAndPassword(String email, String password) throws DAOException {
        try (Session session = DBConnector.getSession()) {
            session.beginTransaction();

            Query<User> query = session.createQuery(QueryUser.findUserByEmailAndPassword(), User.class);
            query.setParameter("email", email);
            query.setParameter("password", password);

            User foundUser = query.uniqueResult();
            session.getTransaction().commit();

            if (foundUser != null) {
                logger.info("User was found!");
                return foundUser;
            }
            logger.info("User was not found!");
            return null;
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
