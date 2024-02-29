package ua.desktop.chat.messenger.client.service.message;

import ua.desktop.chat.messenger.client.exception.UndefinedMessageException;
import ua.desktop.chat.messenger.client.service.chat.ChatHandler;
import ua.desktop.chat.messenger.constant.ChatConstant;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;

import java.util.List;

public class MessageHandler {
    private final ChatHandler chatHandler;

    public MessageHandler(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }

    public List<MessageDTO> getMessagesInChatForUser(ChatDTO chatDTO) throws OpenSessionException {
        chatHandler.validateChatExistence(chatDTO);
        List<ChatDTO> chatORMList = chatHandler.getChatList(chatDTO);
        return getMessageList(chatORMList);
    }

    private List<MessageDTO> getMessageList(List<ChatDTO> chatORMList) throws OpenSessionException {
        return chatHandler.getCommunicationHandler()
                .getMessageSystemHandling()
                .readListMessageByChats(chatORMList);
    }

    public void sendMessage(MessageDTO message) {
        chatHandler.getSocketOutputWriter().println(ChatConstant.MESSAGE_COMMAND);
        chatHandler.getSocketOutputWriter().println((message.getChat().getNameChat().isEmpty()) ? TypeChat.GLOBAL.name() : message.getChat().getNameChat());
        String msg = ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT);
        chatHandler.getSocketOutputWriter().println(msg);
    }

    public synchronized ChatHandler getChatManager() {
        return chatHandler;
    }

}
