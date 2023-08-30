package ua.desktop.chat.messenger.dao.query.hql;

public class QueryChatSystemMessage {
    public static String findChatByChatNameAndUserId() {
        return "FROM Chat c WHERE c.nameChat = :nameChat AND c.user.id = :userId";
    }

    public static String readChatsByUserId() {
        return "FROM Chat c WHERE c.user.id = :userId";
    }
}
