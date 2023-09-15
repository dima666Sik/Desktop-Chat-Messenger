package ua.desktop.chat.messenger.domain.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.dao.exceptions.DAOException;
import ua.desktop.chat.messenger.dao.ifaces.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.dao.util.DAOFactory;
import ua.desktop.chat.messenger.util.Converter;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemHandling;
import ua.desktop.chat.messenger.models.Chat;

import java.util.List;
import java.util.Optional;

public class ChatSystemHandlerImpl implements ChatSystemHandling {
    private final static Logger logger = LogManager.getLogger(ChatSystemHandlerImpl.class.getName());

    private final ChatSystemHandlerDAO chatSystemMessageDAO;

    public ChatSystemHandlerImpl() {
        this.chatSystemMessageDAO = DAOFactory.getChatSystemHandlerDAO();
    }

    @Override
    public boolean isExistChatByUser(String nameChat, Long userId) {
        try {
            return chatSystemMessageDAO.isExistChatByUser(nameChat, userId);
        } catch (DAOException e) {
            logger.warn("Cannot check existing user chat!", e);
            return false;
        }
    }

    @Override
    public void createChatByUser(String nameChat, TypeChat typeChat, UserDTO userDTO, Long idUserCompanion) {
        try {
            chatSystemMessageDAO.createChatByUser(nameChat, typeChat, Converter.convertUserDTOIntoUser(userDTO), idUserCompanion);
        } catch (DAOException e) {
            logger.warn("Cannot create user chat!", e);
        }
    }

    @Override
    public Optional<List<ChatDTO>> readListChatsByUser(UserDTO userDTO) {
        try {
            Optional<List<Chat>> optionalChatList = chatSystemMessageDAO.readListChatsByUser(Converter.convertUserDTOIntoUser(userDTO));
            return optionalChatList.map(Converter::convertListChatDTOIntoListChat);
        } catch (DAOException e) {
            logger.warn("Cannot read chats for user!", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<ChatDTO>> readListChatsByChatName(String nameChat) {
        try {
            Optional<List<Chat>> optionalChatList = chatSystemMessageDAO.readListChatsByChatName(nameChat);
            return optionalChatList.map(Converter::convertListChatDTOIntoListChat);
        } catch (DAOException e) {
            logger.warn("Cannot read chats for user!", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ChatDTO> readChat(String nameChat, Long userId) {
        try {
            Optional<Chat> optionalChat = chatSystemMessageDAO.readChat(nameChat, userId);
            return optionalChat.map(Converter::convertChatIntoChatDTO);
        } catch (DAOException e) {
            logger.warn("Cannot read chats for user!", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ChatDTO> readChatCompanion(ChatDTO chatDTO) {
        try {
            Optional<Chat> optionalChat = chatSystemMessageDAO.readChatCompanion(Converter.convertChatDTOIntoChat(chatDTO));
            return optionalChat.map(Converter::convertChatIntoChatDTO);
        } catch (DAOException e) {
            logger.warn("Cannot read chats for user!", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<ChatDTO>> readChatsByType(TypeChat typeChat, Long userId) {
        try {
            Optional<List<Chat>> optionalChatList = chatSystemMessageDAO.readChatsByType(typeChat, userId);
            return optionalChatList.map(Converter::convertListChatDTOIntoListChat);
        } catch (DAOException e) {
            logger.warn("Cannot read chats for user!", e);
            return Optional.empty();
        }
    }
}
