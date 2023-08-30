package ua.desktop.chat.messenger.domain.client;

import ua.desktop.chat.messenger.closable.ifaces.Closable;
import ua.desktop.chat.messenger.closable.util.ClosableImpl;
import ua.desktop.chat.messenger.domain.ifaces.ClientProvider;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

final public class Client implements ClientProvider {
    private final Closable closable = new ClosableImpl();
    private final Socket socket;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private final Chat chat;

    public Client(Socket socket, Chat chat) {
        try {
            this.socket = socket;
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.chat = chat;

            welcomeClientIntoChat();
        } catch (IOException e) {
            closeClient();
            throw new RuntimeException(e);
        }
    }

    private void welcomeClientIntoChat() {
        User user = chat.getUser();
        String welcomeStringMessage = "SERVER".concat(": ")
                .concat(user.getUsername()
                        .concat(" was added into world chat! Welcome!!!"));


        Message welcomeMessage = new Message(welcomeStringMessage, chat);

        sendMessage(welcomeMessage);

        System.out.println("SERVER".concat(": ")
                .concat(user.getUsername()
                        .concat(" was added into world chat!"))
                .concat("\tTime added into chat: ")
                .concat(String.valueOf(LocalDateTime.now())));
    }

    private void sendSerializedObject(Object object) {
        try {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        } catch (IOException e) {
            closeClient();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(Message message) {
        new Thread(() -> {
            if (socket.isConnected()) {
                sendSerializedObject(message);
            }
        }).start();
    }


    @Override
    public void listenMessageFromClients() {
        new Thread(() -> {

            while (socket.isConnected()) {
                try {
                    byte[] buf = new byte[32 * 1024];
                    int messageFromClientByte = objectInputStream.read(buf);
                    if (messageFromClientByte != -1) {
                        String result = new String(buf, 0, messageFromClientByte);
                        System.out.println(result);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            closeClient();
        }).start();
    }

    public void closeClient() {
//        closable.closeBufferReader(bufferedReader);
        try {
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        closable.closeSocket(socket);
    }
}
