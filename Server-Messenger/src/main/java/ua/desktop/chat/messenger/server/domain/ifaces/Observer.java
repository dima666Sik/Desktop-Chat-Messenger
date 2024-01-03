package ua.desktop.chat.messenger.server.domain.ifaces;

import com.google.common.collect.Multimap;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.UserDTO;

public interface Observer {
    void sendMessage(String msg);
    void sendUserNameList(Multimap<String, ChatDTO> ul);
    String getUsername();
    UserDTO getUserDTO();
}
