package ua.desktop.chat.messenger.dao.query.hql;

public class QueryMessageSystemHandler {
    public static String readMessagesByChatId() {
        return "SELECT M FROM Message M JOIN M.chat C WHERE C.id = :chatId";
    }
}
