package ua.desktop.chat.messenger.core.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.core.service.exception.ProcessingMessageException;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.core.dao.mysql.MessageSystemHandlerDAO;
import ua.desktop.chat.messenger.core.dao.util.DAOFactory;
import ua.desktop.chat.messenger.core.service.MessageSystemHandling;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.model.Message;
import ua.desktop.chat.messenger.domain.util.Mapper;

import java.util.List;

public class MessageSystemHandlerImpl implements MessageSystemHandling {
    private static final Logger logger = LogManager.getLogger(MessageSystemHandlerImpl.class.getName());

    private final MessageSystemHandlerDAO messageSystemHandlerDAO;

    public MessageSystemHandlerImpl() {
        this.messageSystemHandlerDAO = DAOFactory.createMessageSystemHandlerDAO();
    }

    @Override
    public void createMessageByChat(MessageDTO message) {
        try {
            messageSystemHandlerDAO.createMessageByChat(Mapper.convertMessageDTOIntoMessage(message));
        } catch (OpenSessionException e) {
            logger.warn("Cannot create user message in the chat `".concat(message.getChat().getNameChat()).concat("`!"), e);
            throw new ProcessingMessageException("Cannot create user message in the chat `".concat(message.getChat().getNameChat()).concat("`!"), e);
        }
    }

    @Override
    public List<MessageDTO> readListMessageByChats(List<ChatDTO> chatList) {
        try {
            List<Message> messageDTOList = messageSystemHandlerDAO.readListMessageByChats(Mapper.convertListChatDTOIntoListChat(chatList));
            return Mapper.convertListMessageIntoListMessageDTO(messageDTOList);
        } catch (OpenSessionException e) {
            logger.warn("Cannot read messages for chats!", e);
            throw new ProcessingMessageException("Cannot read messages for chats!", e);
        }
    }
}
