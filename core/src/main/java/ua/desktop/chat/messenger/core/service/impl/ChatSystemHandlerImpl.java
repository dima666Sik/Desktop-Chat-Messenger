package ua.desktop.chat.messenger.core.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.core.service.exception.ProcessingChatException;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.core.dao.mysql.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.core.dao.util.DAOFactory;
import ua.desktop.chat.messenger.domain.util.Mapper;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.model.Chat;

import java.util.List;
import java.util.Optional;

public class ChatSystemHandlerImpl implements ChatSystemHandling {
    private static final Logger logger = LogManager.getLogger(ChatSystemHandlerImpl.class.getName());

    private final ChatSystemHandlerDAO chatSystemMessageDAO;

    public ChatSystemHandlerImpl() {
        chatSystemMessageDAO = DAOFactory.createChatSystemHandlerDAO();
    }

    @Override
    public boolean isExistChatByUser(String nameChat, Long userId) throws OpenSessionException {
        try {
            Optional<Chat> foundChat = chatSystemMessageDAO.findChatByChatNameAndUserId(nameChat, userId);

            if (foundChat.isPresent()) {
                logger.info("Chat with this name `{}` was existed!", nameChat);
                return true;
            }

            logger.info("Chat with this name `{}` was not existed!", nameChat);
            return false;
        } catch (OpenSessionException e) {
            logger.warn("Cannot check existing user chat!", e);
            throw e;
        }
    }

    @Override
    public boolean createChatByUser(String nameChat, TypeChat typeChat, UserDTO userDTO, Long idUserCompanion)
            throws OpenSessionException {
        try {
            return chatSystemMessageDAO.createChatByUser(nameChat, typeChat, Mapper.convertUserDTOIntoUser(userDTO), idUserCompanion);
        } catch (OpenSessionException e) {
            logger.warn("Cannot create user chat!", e);
            throw e;
        }
    }

    @Override
    public List<ChatDTO> readListChatsByUser(UserDTO userDTO) throws OpenSessionException {
        try {
            List<Chat> chatList = chatSystemMessageDAO.readListChatsByUser(Mapper.convertUserDTOIntoUser(userDTO));
            return Mapper.convertListChatIntoListChatDTO(chatList);
        } catch (OpenSessionException e) {
            logger.warn("Cannot read chats for user!", e);
            throw e;
        }
    }

    @Override
    public List<ChatDTO> readListChatsByChatName(String nameChat) throws OpenSessionException {
        try {
            List<Chat> chatList = chatSystemMessageDAO.readListChatsByChatName(nameChat);
            return Mapper.convertListChatIntoListChatDTO(chatList);
        } catch (OpenSessionException e) {
            logger.warn("Cannot read chats for user!", e);
            throw e;
        }
    }

    @Override
    public Optional<ChatDTO> readChat(String nameChat, Long userId) throws OpenSessionException {
        try {
            var optionalChat = chatSystemMessageDAO.findChatByChatNameAndUserId(nameChat, userId);
            logger.info("Read chats was successful!");
            return optionalChat.map(Mapper::convertChatIntoChatDTO);
        } catch (OpenSessionException e) {
            logger.warn("Cannot read chat for user!", e);
            throw e;
        }
    }

    @Override
    public Optional<ChatDTO> readChatCompanion(ChatDTO chatDTO) throws OpenSessionException {
        try {
            Optional<Chat> optionalChat = chatSystemMessageDAO.readChatCompanion(Mapper.convertChatDTOIntoChat(chatDTO));
            return optionalChat.map(Mapper::convertChatIntoChatDTO);
        } catch (OpenSessionException e) {
            logger.warn("Cannot read chat for user!", e);
            throw e;
        }
    }

    @Override
    public List<ChatDTO> readChatsByType(TypeChat typeChat, Long userId) throws OpenSessionException {
        try {
            List<Chat> chatList = chatSystemMessageDAO.readChatsByType(typeChat, userId);
            return Mapper.convertListChatIntoListChatDTO(chatList);
        } catch (OpenSessionException e) {
            logger.warn("Cannot read chat for user!", e);
            throw e;
        }
    }
}
