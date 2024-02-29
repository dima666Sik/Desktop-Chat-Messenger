package ua.desktop.chat.messenger.core.service;

import ua.desktop.chat.messenger.core.service.exception.ProcessingChatException;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;

import java.util.List;
import java.util.Optional;

public interface ChatSystemHandling {
    boolean isExistChatByUser(String nameChat, Long userId) throws OpenSessionException;

    boolean createChatByUser(String nameChat, TypeChat typeChat, UserDTO userDTO, Long idUserCompanion) throws OpenSessionException;

    List<ChatDTO> readListChatsByUser(UserDTO userDTO) throws OpenSessionException;

    List<ChatDTO> readListChatsByChatName(String nameChat) throws OpenSessionException;

    Optional<ChatDTO> readChat(String nameChat, Long userId) throws OpenSessionException;
    Optional<ChatDTO> readChatCompanion(ChatDTO chat) throws OpenSessionException;

    List<ChatDTO> readChatsByType(TypeChat typeChat, Long userId) throws OpenSessionException;
}
