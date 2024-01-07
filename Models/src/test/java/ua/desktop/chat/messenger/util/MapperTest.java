package ua.desktop.chat.messenger.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.model.Chat;
import ua.desktop.chat.messenger.domain.model.Message;
import ua.desktop.chat.messenger.domain.model.User;
import ua.desktop.chat.messenger.domain.util.Mapper;

import java.time.LocalDateTime;
import java.util.List;

class MapperTest {
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
        Assertions.assertEquals(userDTO, Mapper.convertUserIntoUserDTO(user));
    }

    @Test
    void convertUserDTOIntoUser() {
        Assertions.assertEquals(user, Mapper.convertUserDTOIntoUser(userDTO));
    }

    @Test
    void convertChatDTOIntoChat() {
        Assertions.assertEquals(chatGroup, Mapper.convertChatDTOIntoChat(chatDTOGroup));
    }

    @Test
    void convertChatIntoChatDTO() {
        Assertions.assertEquals(chatDTOGroup, Mapper.convertChatIntoChatDTO(chatGroup));
    }

    @Test
    void convertMessageDTOIntoMessage() {
        Assertions.assertEquals(message, Mapper.convertMessageDTOIntoMessage(messageDTO));
    }

    @Test
    void convertMessageIntoMessageDTO() {
        Assertions.assertEquals(messageDTO, Mapper.convertMessageIntoMessageDTO(message));
    }

    @Test
    void convertListChatDTOIntoListChat() {
        Assertions.assertEquals(List.of(chatGroup), Mapper.convertListChatDTOIntoListChat(List.of(chatDTOGroup)));
    }

    @Test
    void convertListChatIntoListChatDTO() {
        Assertions.assertEquals(List.of(chatDTOGroup), Mapper.convertListChatIntoListChatDTO(List.of(chatGroup)));
    }

    @Test
    void convertListMessageIntoListMessageDTO() {
        Assertions.assertEquals(List.of(messageDTO), Mapper.convertListMessageIntoListMessageDTO(List.of(message)));
    }
}