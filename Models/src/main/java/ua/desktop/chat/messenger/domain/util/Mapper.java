package ua.desktop.chat.messenger.domain.util;

import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.Message;
import ua.desktop.chat.messenger.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class Mapper {
    private Mapper(){}
    public static UserDTO convertUserIntoUserDTO(User user) {
        return new UserDTO(user);
    }

    public static User convertUserDTOIntoUser(UserDTO user) {
        return new User(user);
    }

    public static Chat convertChatDTOIntoChat(ChatDTO chatDTO) {
        return new Chat(chatDTO.getId(), chatDTO.getNameChat(), chatDTO.getTypeChat(), convertUserDTOIntoUser(chatDTO.getUser()), chatDTO.getUserCompanionId());
    }

    public static ChatDTO convertChatIntoChatDTO(Chat chat) {
        return new ChatDTO(chat.getId(), chat.getNameChat(), chat.getTypeChat(), convertUserIntoUserDTO(chat.getUser()), chat.getUserCompanionId());
    }

    public static Message convertMessageDTOIntoMessage(MessageDTO messageDTO) {
        Chat chat = convertChatDTOIntoChat(messageDTO.getChat());
        return new Message(messageDTO, chat);
    }

    public static MessageDTO convertMessageIntoMessageDTO(Message message) {
        ChatDTO chatDTO = convertChatIntoChatDTO(message.getChat());
        return new MessageDTO(message.getMessage(), message.getLocalDateTime(), chatDTO);
    }

    public static List<ChatDTO> convertListChatIntoListChatDTO(List<Chat> chatList) {
        List<ChatDTO> chatDTOList = new ArrayList<>();
        chatList.forEach(e -> chatDTOList.add(convertChatIntoChatDTO(e)));
        return chatDTOList;
    }

    public static List<Chat> convertListChatDTOIntoListChat(List<ChatDTO> chatDTOList) {
        List<Chat> chatList = new ArrayList<>();
        chatDTOList.forEach(e -> chatList.add(convertChatDTOIntoChat(e)));
        return chatList;
    }

    public static List<MessageDTO> convertListMessageIntoListMessageDTO(List<Message> messageList) {
        List<MessageDTO> messageDTOList = new ArrayList<>();
        messageList.forEach(e -> messageDTOList.add(convertMessageIntoMessageDTO(e)));
        return messageDTOList;
    }
}
