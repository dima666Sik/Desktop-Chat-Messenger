package ua.desktop.chat.messenger.auth.dao.query.hql;

public class QueryUser {
    private QueryUser(){}
    public static final String FIND_USER_BY_EMAIL_AND_PASSWORD =
            "FROM User u WHERE u.email = :email AND u.password = :password";

    public static final String FIND_USER_BY_EMAIL_OR_USERNAME =
            "FROM User u WHERE u.email = :email OR u.username = :username";
}
