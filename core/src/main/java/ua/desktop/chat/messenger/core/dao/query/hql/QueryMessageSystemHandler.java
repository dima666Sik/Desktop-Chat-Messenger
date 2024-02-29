package ua.desktop.chat.messenger.core.dao.query.hql;

public class QueryMessageSystemHandler {
    private QueryMessageSystemHandler(){}
    public static final String READ_MESSAGES_BY_CHAT_ID = "SELECT M FROM Message M JOIN M.chat C WHERE C.id = :chatId";
}
