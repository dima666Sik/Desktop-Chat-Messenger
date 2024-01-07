package ua.desktop.chat.messenger.core.service;

import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;

import java.util.List;

public interface MessageSystemHandling {
    void createMessageByChat(MessageDTO message);
    List<MessageDTO> readListMessageByChats(List<ChatDTO> chatList);
}
