package ua.desktop.chat.messenger.core.dao.query.hql;

public class QueryChatSystemHandler {
    public static String findChatByChatNameAndUserId() {
        return "FROM Chat c WHERE c.nameChat = :nameChat AND c.user.id = :userId";
    }

    public static String readChatsByUserId() {
        return "FROM Chat c WHERE c.user.id = :userId";
    }

    public static String findChatsByName() {
        return "FROM Chat c WHERE c.nameChat = :nameChat";
    }

    public static String findChatCompanionByUserCompanionIdAndUserId() {
        return "FROM Chat c WHERE c.userCompanionId = :userCompanionId AND c.user.id = :userId";
    }

    public static String findChatsByTypeChatAndUserId() {
        return "FROM Chat c WHERE c.typeChat = :typeChat AND c.user.id = :userId";
    }
}
