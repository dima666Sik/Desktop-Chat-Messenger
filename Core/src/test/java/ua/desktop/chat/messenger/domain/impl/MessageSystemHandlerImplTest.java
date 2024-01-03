package ua.desktop.chat.messenger.domain.impl;

import org.junit.jupiter.api.*;
import ua.desktop.chat.messenger.core.dao.util.Encryption;
import ua.desktop.chat.messenger.core.domain.exceptions.DomainException;
import ua.desktop.chat.messenger.core.domain.ifaces.MessageSystemHandling;
import ua.desktop.chat.messenger.core.domain.impl.MessageSystemHandlerImpl;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageSystemHandlerImplTest {
    private static MessageSystemHandling messageSystemHandling;
    private static UserDTO userDTO;
    private static ChatDTO chatDTOGroup;
    private static MessageDTO messageDTO;

    @BeforeAll
    static void setUp() throws DomainException {
        messageSystemHandling = new MessageSystemHandlerImpl();
        userDTO = new UserDTO(6L, "devil", "devil", Encryption.encryptionSHA3256("devil"));
        chatDTOGroup = new ChatDTO(7L, "Termopasta Grizli", TypeChat.GROUP, userDTO, null);
        messageDTO = new MessageDTO("Heeelp mee this is test msg...", LocalDateTime.now(), chatDTOGroup);
    }

    @Test
    @Order(1)
    void createMessageByChat() {
        Assertions.assertTrue(messageSystemHandling.createMessageByChat(messageDTO));
    }

    @Test
    @Order(2)
    void readListMessageByChats() {
        Optional<List<MessageDTO>> optionalMessageDTOList = messageSystemHandling.readListMessageByChats(List.of(chatDTOGroup));
        if (optionalMessageDTOList.isEmpty()) fail();
        Assertions.assertNotNull(optionalMessageDTOList.get().toArray());
    }
}