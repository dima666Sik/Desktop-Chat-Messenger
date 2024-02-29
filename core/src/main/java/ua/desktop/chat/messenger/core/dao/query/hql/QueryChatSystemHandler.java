package ua.desktop.chat.messenger.core.dao.query.hql;

public class QueryChatSystemHandler {
    private QueryChatSystemHandler(){}
    public static final String FIND_CHAT_BY_CHAT_NAME_AND_USER_ID = "FROM Chat c WHERE c.nameChat = :nameChat AND c.user.id = :userId";
    public static final String READ_CHATS_BY_USER_ID = "FROM Chat c WHERE c.user.id = :userId";
    public static final String FIND_CHATS_BY_NAME = "FROM Chat c WHERE c.nameChat = :nameChat";
    public static final String FIND_CHAT_COMPANION_BY_USER_COMPANION_ID_AND_USER_ID = "FROM Chat c WHERE c.userCompanionId = :userCompanionId AND c.user.id = :userId";
    public static final String FIND_CHATS_BY_TYPE_CHAT_AND_USER_ID = "FROM Chat c WHERE c.typeChat = :typeChat AND c.user.id = :userId";
}
