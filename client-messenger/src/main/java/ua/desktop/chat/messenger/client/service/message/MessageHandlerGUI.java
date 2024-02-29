package ua.desktop.chat.messenger.client.service.message;

import ua.desktop.chat.messenger.client.ui.chat.ChatMessengerGUI;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;

import java.time.format.DateTimeFormatter;

public class MessageHandlerGUI {
    private final ChatMessengerGUI windowChatMessenger;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    public MessageHandlerGUI(ChatMessengerGUI windowChatMessenger) {
        this.windowChatMessenger = windowChatMessenger;
    }

    public void setMessage(MessageDTO msg) {
        msg.setMessage(String.format("(%s)%s", msg.getLocalDateTime().format(formatter), msg.getMessage()));
        windowChatMessenger.updateChat(msg);
    }

    public void updateMessageChatGUI(String msg) {
        windowChatMessenger.updateChat(msg);
    }

}
