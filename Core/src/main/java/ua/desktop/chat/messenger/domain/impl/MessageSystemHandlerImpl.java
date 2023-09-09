package ua.desktop.chat.messenger.domain.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.dao.ifaces.MessageSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.util.DAOFactory;
import ua.desktop.chat.messenger.domain.ifaces.MessageSystemHandling;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;

import java.util.List;

public class MessageSystemHandlerImpl implements MessageSystemHandling {
    private final static Logger logger = LogManager.getLogger(MessageSystemHandlerImpl.class.getName());

    private final MessageSystemHandlerDAO messageSystemHandlerDAO;

    public MessageSystemHandlerImpl() {
        this.messageSystemHandlerDAO = DAOFactory.getMessageSystemHandlerDAO();
    }

    @Override
    public Message createMessageByChat(Message message) {
        try {
            return messageSystemHandlerDAO.createMessageByChat(message);
        } catch (DAOException e) {
            logger.warn("Cannot create user message in the chat `".concat(message.getChat().getNameChat()).concat("`!"), e);
            return null;
        }
    }

    @Override
    public List<Message> readListMessageByChats(List<Chat> chatList) {
        try {
            return messageSystemHandlerDAO.readListMessageByChats(chatList);
        } catch (DAOException e) {
            logger.warn("Cannot read chats for user!", e);
            return null;
        }
    }
}
