package ua.desktop.chat.messenger.core.service;

import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;

import java.util.List;
import java.util.Optional;

public interface ChatSystemHandling {
    boolean isExistChatByUser(String nameChat, Long userId);

    boolean createChatByUser(String nameChat, TypeChat typeChat, UserDTO userDTO, Long idUserCompanion);

    List<ChatDTO> readListChatsByUser(UserDTO userDTO);

    List<ChatDTO> readListChatsByChatName(String nameChat);

    Optional<ChatDTO> readChat(String nameChat, Long userId);
    Optional<ChatDTO> readChatCompanion(ChatDTO chat);

    List<ChatDTO> readChatsByType(TypeChat typeChat, Long userId);
}
