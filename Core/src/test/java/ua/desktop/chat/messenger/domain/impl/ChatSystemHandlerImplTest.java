package ua.desktop.chat.messenger.domain.impl;

import org.junit.jupiter.api.*;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.core.service.impl.ChatSystemHandlerImpl;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.encryption.Encryption;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.exception.EncryptionException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatSystemHandlerImplTest {
    private static ChatSystemHandling chatSystemHandling;
    private static UserDTO userDTO1;
    private static UserDTO userDTO2;
    private static ChatDTO chatDTOGroup;
    private static ChatDTO chatDTOPrivate1;
    private static ChatDTO chatDTOPrivate2;


    @BeforeAll
    static void setUp() throws EncryptionException {
        chatSystemHandling = new ChatSystemHandlerImpl();
        userDTO1 = new UserDTO(6L, "devil", "devil", Encryption.encryptionSHA3256("devil"));
        userDTO2 = new UserDTO(7L, "angel", "angel", Encryption.encryptionSHA3256("angel"));
        chatDTOGroup = new ChatDTO(7L, "Termopasta Grizli", TypeChat.GROUP, userDTO1, null);
        chatDTOPrivate1 = new ChatDTO(8L, userDTO2.getUsername(), TypeChat.PRIVATE, userDTO1, userDTO2.getId());
        chatDTOPrivate2 = new ChatDTO(9L, userDTO1.getUsername(), TypeChat.PRIVATE, userDTO2, userDTO1.getId());
    }

    @Test
    @Order(2)
    void isExistChatByUser() {
        assertTrue(chatSystemHandling.isExistChatByUser(chatDTOGroup.getNameChat(), chatDTOGroup.getUser().getId()));
    }

    @Test
    @Order(1)
    void createChatByUser() {
        assertTrue(chatSystemHandling.createChatByUser(chatDTOGroup.getNameChat(), chatDTOGroup.getTypeChat(), chatDTOGroup.getUser(), chatDTOGroup.getUserCompanionId()));
        assertTrue(chatSystemHandling.createChatByUser(chatDTOPrivate1.getNameChat(), chatDTOPrivate1.getTypeChat(), chatDTOPrivate1.getUser(), chatDTOPrivate1.getUserCompanionId()));
        assertTrue(chatSystemHandling.createChatByUser(chatDTOPrivate2.getNameChat(), chatDTOPrivate2.getTypeChat(), chatDTOPrivate2.getUser(), chatDTOPrivate2.getUserCompanionId()));
    }

    @Test
    @Order(6)
    void readListChatsByUser() {
        List<ChatDTO> chatDTO = chatSystemHandling.readListChatsByUser(userDTO1);
        if (chatDTO.isEmpty()) fail();
        Assertions.assertEquals(List.of(chatDTOGroup, chatDTOPrivate1), chatDTO);
    }

    @Test
    @Order(7)
    void readListChatsByChatName() {
        List<ChatDTO> chatDTO = chatSystemHandling.readListChatsByChatName(chatDTOGroup.getNameChat());
        if (chatDTO.isEmpty()) fail();
        Assertions.assertEquals(List.of(chatDTOGroup), chatDTO);
    }

    @Test
    @Order(3)
    void readChat() {
        Optional<ChatDTO> optionalChatDTO = chatSystemHandling.readChat(chatDTOGroup.getNameChat(), chatDTOGroup.getUser().getId());
        if (optionalChatDTO.isEmpty()) fail();
        assertEquals(chatDTOGroup, optionalChatDTO.get());
    }

    @Test
    @Order(4)
    void readChatCompanion() {
        Optional<ChatDTO> optionalChatDTO = chatSystemHandling.readChatCompanion(chatDTOPrivate1);
        if (optionalChatDTO.isEmpty()) fail();
        Assertions.assertEquals(chatDTOPrivate2, optionalChatDTO.get());
    }

    @Test
    @Order(5)
    void readChatsByType() {
        List<ChatDTO> chatDTO = chatSystemHandling.readChatsByType(chatDTOGroup.getTypeChat(), userDTO1.getId());
        if (chatDTO.isEmpty()) fail();
        Assertions.assertEquals(List.of(chatDTOGroup), chatDTO);
    }
}