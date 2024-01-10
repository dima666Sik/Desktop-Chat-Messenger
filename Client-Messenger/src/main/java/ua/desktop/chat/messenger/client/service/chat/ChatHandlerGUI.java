package ua.desktop.chat.messenger.client.service.chat;

import com.google.common.collect.Multimap;
import ua.desktop.chat.messenger.client.service.CommunicationHandler;
import ua.desktop.chat.messenger.client.service.message.MessageHandlerGUI;
import ua.desktop.chat.messenger.client.ui.chat.ChatMessengerGUI;
import ua.desktop.chat.messenger.constant.ChatConstant;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.io.BufferedReader;
import java.io.IOException;

public class ChatHandlerGUI {
    private final MessageHandlerGUI messageHandlerGUI;
    private final ChatMessengerGUI windowChatMessenger;
    private final ChatHandler chatHandler;

    public ChatHandlerGUI(MessageHandlerGUI messageHandlerGUI,
                          ChatMessengerGUI windowChatMessenger,
                          ChatHandler chatHandler) {
        this.messageHandlerGUI = messageHandlerGUI;
        this.windowChatMessenger = windowChatMessenger;
        this.chatHandler = chatHandler;
    }

    public void updateUserListChatGUI(Multimap<String, ChatDTO> chats) {
        windowChatMessenger.addChatList(chats);
    }

    public void updateMessageChatGUI(MessageDTO msg) {

        if (msg.getChat().getTypeChat() == TypeChat.GLOBAL || msg.getChat().getTypeChat() == TypeChat.GROUP) {
            if (!msg.getMessage().substring(9, 9 + chatHandler.getUser().getUsername().length())
                    .equals(chatHandler.getUser().getUsername())) {
                messageHandlerGUI.setMessage(msg);
            }
        } else {
            messageHandlerGUI.setMessage(msg);
        }
    }

    public void processResponse() throws IOException {
        System.out.println(Thread.currentThread());
        String response;
        while (chatHandler.getSocketInputReader() != null
                && (response = chatHandler
                .getSocketInputReader()
                .readLine()) != null) {
            if (response.equals(ChatConstant.USERS_COMMAND)) {
                processUserListResponse();
            } else if (response.equals(ChatConstant.MESSAGE_COMMAND)) {
                processMessageResponse();
            }
        }
    }

    private void processUserListResponse() throws IOException {
        String userList;
        if ((userList = chatHandler
                .getSocketInputReader()
                .readLine()) != null
                && !userList.equals(ChatConstant.USERS_COMMAND)) {
            Multimap<String, ChatDTO> users = chatHandler.setUserList(userList);
            updateUserListChatGUI(users);
            return;
        }
        processUserListResponse();
    }

    private void processMessageResponse() throws IOException {
        String messageResponse;
        if ((messageResponse = chatHandler
                .getSocketInputReader()
                .readLine()) != null) {
            Object parsedObject = ParserJSON.convertStringToObject(messageResponse);

            if (parsedObject instanceof MessageDTO) {
                MessageDTO message = (MessageDTO) parsedObject;
                updateMessageChatGUI(message);
            } else if (parsedObject instanceof String) {
                String textMessage = (String) parsedObject;
                messageHandlerGUI.updateMessageChatGUI(textMessage);
            }
        }
    }
}
