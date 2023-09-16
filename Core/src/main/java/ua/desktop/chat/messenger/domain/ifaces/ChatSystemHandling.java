package ua.desktop.chat.messenger.domain.ifaces;

import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.util.List;
import java.util.Optional;

public interface ChatSystemHandling {
    boolean isExistChatByUser(String nameChat, Long userId);

    boolean createChatByUser(String nameChat, TypeChat typeChat, UserDTO userDTO, Long idUserCompanion);

    Optional<List<ChatDTO>> readListChatsByUser(UserDTO userDTO);

    Optional<List<ChatDTO>> readListChatsByChatName(String nameChat);

    Optional<ChatDTO> readChat(String nameChat, Long userId);
    Optional<ChatDTO> readChatCompanion(ChatDTO chat);

    Optional<List<ChatDTO>> readChatsByType(TypeChat typeChat, Long userId);
}
