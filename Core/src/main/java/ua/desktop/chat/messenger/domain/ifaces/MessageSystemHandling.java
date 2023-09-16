package ua.desktop.chat.messenger.domain.ifaces;

import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;

import java.util.List;
import java.util.Optional;

public interface MessageSystemHandling {
    boolean createMessageByChat(MessageDTO message);
    Optional<List<MessageDTO>> readListMessageByChats(List<ChatDTO> chatList);
}
