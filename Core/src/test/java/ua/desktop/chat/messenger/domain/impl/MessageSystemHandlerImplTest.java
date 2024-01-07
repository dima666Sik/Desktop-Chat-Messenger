package ua.desktop.chat.messenger.domain.impl;

import org.junit.jupiter.api.*;
import ua.desktop.chat.messenger.core.service.MessageSystemHandling;
import ua.desktop.chat.messenger.core.service.impl.MessageSystemHandlerImpl;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.encryption.Encryption;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.exception.EncryptionException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageSystemHandlerImplTest {
    private static MessageSystemHandling messageSystemHandling;
    private static UserDTO userDTO;
    private static ChatDTO chatDTOGroup;
    private static MessageDTO messageDTO;

    @BeforeAll
    static void setUp() throws EncryptionException {
        messageSystemHandling = new MessageSystemHandlerImpl();
        userDTO = new UserDTO(6L, "devil", "devil", Encryption.encryptionSHA3256("devil"));
        chatDTOGroup = new ChatDTO(7L, "Termopasta Grizli", TypeChat.GROUP, userDTO, null);
        messageDTO = new MessageDTO("Heeelp mee this is test msg...", LocalDateTime.now(), chatDTOGroup);
    }

    @Test
    @Order(1)
    void createMessageByChat() {
        Assertions.assertDoesNotThrow(() -> messageSystemHandling.createMessageByChat(messageDTO));
    }

    @Test
    @Order(2)
    void readListMessageByChats() {
        List<MessageDTO> mesageDTOList = messageSystemHandling.readListMessageByChats(List.of(chatDTOGroup));
        if (mesageDTOList.isEmpty()) fail();
        Assertions.assertNotNull(mesageDTOList.toArray());
    }
}