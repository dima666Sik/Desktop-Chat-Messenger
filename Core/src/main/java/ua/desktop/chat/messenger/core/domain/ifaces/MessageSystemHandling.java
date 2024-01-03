package ua.desktop.chat.messenger.core.domain.ifaces;

import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;

import java.util.List;
import java.util.Optional;

public interface MessageSystemHandling {
    boolean createMessageByChat(MessageDTO message);
    Optional<List<MessageDTO>> readListMessageByChats(List<ChatDTO> chatList);
}
