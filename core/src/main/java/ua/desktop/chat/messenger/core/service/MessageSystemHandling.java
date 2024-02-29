package ua.desktop.chat.messenger.core.service;

import ua.desktop.chat.messenger.core.service.exception.ProcessingMessageException;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;

import java.util.List;

public interface MessageSystemHandling {
    boolean createMessageByChat(MessageDTO message) throws OpenSessionException;
    List<MessageDTO> readListMessageByChats(List<ChatDTO> chatList) throws OpenSessionException;
}
