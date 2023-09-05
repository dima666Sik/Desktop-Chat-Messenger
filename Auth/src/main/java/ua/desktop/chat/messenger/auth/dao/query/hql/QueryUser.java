package ua.desktop.chat.messenger.auth.dao.query.hql;

public class QueryUser {
    public static String findUserByEmailAndPassword() {
        return "FROM User u WHERE u.email = :email AND u.password = :password";
    }
    public static String findUserByEmail() {
        return "FROM User u WHERE u.email = :email";
    }

}
