package ua.desktop.chat.messenger.domain.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.dao.ifaces.MessageSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.util.DAOFactory;
import ua.desktop.chat.messenger.domain.ifaces.MessageSystemHandling;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.util.Converter;

import java.util.List;
import java.util.Optional;

public class MessageSystemHandlerImpl implements MessageSystemHandling {
    private final static Logger logger = LogManager.getLogger(MessageSystemHandlerImpl.class.getName());

    private final MessageSystemHandlerDAO messageSystemHandlerDAO;

    public MessageSystemHandlerImpl() {
        this.messageSystemHandlerDAO = DAOFactory.getMessageSystemHandlerDAO();
    }

    @Override
    public boolean createMessageByChat(MessageDTO message) {
        try {
            return messageSystemHandlerDAO.createMessageByChat(Converter.convertMessageDTOIntoMessage(message));
        } catch (DAOException e) {
            logger.warn("Cannot create user message in the chat `".concat(message.getChat().getNameChat()).concat("`!"), e);
            return false;
        }
    }

    @Override
    public Optional<List<MessageDTO>> readListMessageByChats(List<ChatDTO> chatList) {
        try {
            Optional<List<Message>> optionalMessageDTOList = messageSystemHandlerDAO.readListMessageByChats(Converter.convertListChatDTOIntoListChat(chatList));
            return optionalMessageDTOList.map(Converter::convertListMessageIntoListMessageDTO);
        } catch (DAOException e) {
            logger.warn("Cannot read messages for chats!", e);
            return Optional.empty();
        }
    }
}
