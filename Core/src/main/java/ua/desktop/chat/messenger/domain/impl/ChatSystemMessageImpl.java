package ua.desktop.chat.messenger.domain.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.dao.ifaces.ChatSystemMessageDAO;
import ua.desktop.chat.messenger.dao.util.DAOFactory;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemMessaging;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;

public class ChatSystemMessageImpl implements ChatSystemMessaging {
    private final static Logger logger = LogManager.getLogger(ChatSystemMessageImpl.class.getName());

    private final ChatSystemMessageDAO chatSystemMessageDAO;

    public ChatSystemMessageImpl() {
        this.chatSystemMessageDAO = DAOFactory.getChatSystemMessageDAO();
    }

    @Override
    public boolean isExistChatByUser(String nameChat, User user) {
        try {
            return chatSystemMessageDAO.isExistChatByUser(nameChat, user);
        } catch (DAOException e) {
            logger.warn("Cannot check existing user chat!", e);
            return false;
        }
    }

    @Override
    public Chat createChatByUser(String nameChat, TypeChat typeChat, User user) {
        try {
            return chatSystemMessageDAO.createChatByUser(nameChat,typeChat, user);
        } catch (DAOException e) {
            logger.warn("Cannot create user chat!", e);
            return null;
        }
    }

    @Override
    public List<Chat> readListChatsByUser(User user) {
        try {
            return chatSystemMessageDAO.readListChatsByUser(user);
        } catch (DAOException e) {
            logger.warn("Cannot read chats for user!", e);
            return null;
        }
    }
}
