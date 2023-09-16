package ua.desktop.chat.messenger.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

import java.time.LocalDateTime;
import java.util.List;

class ConverterTest {
    private static User user;
    private static Chat chatGroup;
    private static Message message;

    private static UserDTO userDTO;
    private static ChatDTO chatDTOGroup;
    private static MessageDTO messageDTO;

    @BeforeAll
    static void setUp() {
        userDTO = new UserDTO(6L, "devil", "devil", "devil");
        chatDTOGroup = new ChatDTO(7L, "Termopasta Grizli", TypeChat.GROUP, userDTO, null);
        messageDTO = new MessageDTO("Heeelp mee this is test msg...", LocalDateTime.now(), chatDTOGroup);

        user = new User(userDTO);
        chatGroup = new Chat(7L, chatDTOGroup.getNameChat(), chatDTOGroup.getTypeChat(), user, chatDTOGroup.getUserCompanionId());
        message = new Message(messageDTO.getMessage(), messageDTO.getLocalDateTime(), chatGroup);
    }

    @Test
    void convertUserIntoUserDTO() {
        Assertions.assertEquals(userDTO, Converter.convertUserIntoUserDTO(user));
    }

    @Test
    void convertUserDTOIntoUser() {
        Assertions.assertEquals(user, Converter.convertUserDTOIntoUser(userDTO));
    }

    @Test
    void convertChatDTOIntoChat() {
        Assertions.assertEquals(chatGroup, Converter.convertChatDTOIntoChat(chatDTOGroup));
    }

    @Test
    void convertChatIntoChatDTO() {
        Assertions.assertEquals(chatDTOGroup, Converter.convertChatIntoChatDTO(chatGroup));
    }

    @Test
    void convertMessageDTOIntoMessage() {
        Assertions.assertEquals(message, Converter.convertMessageDTOIntoMessage(messageDTO));
    }

    @Test
    void convertMessageIntoMessageDTO() {
        Assertions.assertEquals(messageDTO, Converter.convertMessageIntoMessageDTO(message));
    }

    @Test
    void convertListChatDTOIntoListChat() {
        Assertions.assertEquals(List.of(chatGroup), Converter.convertListChatDTOIntoListChat(List.of(chatDTOGroup)));
    }

    @Test
    void convertListChatIntoListChatDTO() {
        Assertions.assertEquals(List.of(chatDTOGroup), Converter.convertListChatIntoListChatDTO(List.of(chatGroup)));
    }

    @Test
    void convertListMessageIntoListMessageDTO() {
        Assertions.assertEquals(List.of(messageDTO), Converter.convertListMessageIntoListMessageDTO(List.of(message)));
    }
}