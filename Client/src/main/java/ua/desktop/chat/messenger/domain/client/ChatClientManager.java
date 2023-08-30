package ua.desktop.chat.messenger.domain.client;

import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import java.io.IOException;
import java.net.Socket;

public class ChatClientManager {
    public static synchronized Client getInstance(Chat chat) {
        try {
            Socket s = new Socket("localhost", 8086);
            return new Client(s, chat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
