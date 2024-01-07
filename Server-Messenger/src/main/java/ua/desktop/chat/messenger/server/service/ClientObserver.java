package ua.desktop.chat.messenger.server.service;

import com.google.common.collect.Multimap;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;

public interface ClientObserver {
    void sendMessage(String msg);
    void sendUserNameList(Multimap<String, ChatDTO> ul);
    String getUsername();
    UserDTO getUserDTO();
}
