package ua.desktop.chat.messenger.core.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ua.desktop.chat.messenger.core.dao.mysql.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.core.dao.mysql.MessageSystemHandlerDAO;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.model.Message;
import ua.desktop.chat.messenger.domain.util.Mapper;
import ua.desktop.chat.messenger.encryption.Encryption;
import ua.desktop.chat.messenger.exception.EncryptionException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageSystemHandlerImplTest {

    private static MessageSystemHandlerImpl messageSystemHandler;
    private static MessageSystemHandlerDAO messageSystemHandlerDAO;
    private static UserDTO userDto;
    private static ChatDTO chatDtoGroup;

    private static MessageDTO messageDto;

    @BeforeEach
    void setupUp() throws EncryptionException {
        messageSystemHandler = new MessageSystemHandlerImpl();
        messageSystemHandlerDAO = Mockito.mock(MessageSystemHandlerDAO.class);

        userDto = new UserDTO(1L, "devil", "devil", Encryption.encryptionSHA3256("devil"));

        chatDtoGroup = new ChatDTO(7L, "Test Termopasta Grizli", TypeChat.GROUP, userDto, null);

        messageDto = new MessageDTO("Heeelp mee this is test msg...", LocalDateTime.now(), chatDtoGroup);
    }

    void reflectInitMockFactoryForService(MessageSystemHandlerDAO messageSystemHandlerDAO)
            throws NoSuchFieldException, IllegalAccessException {
        var coreServiceClass = messageSystemHandler.getClass();
        var fieldUserDao = coreServiceClass.getDeclaredField("messageSystemHandlerDAO");
        fieldUserDao.setAccessible(true);
        fieldUserDao.set(messageSystemHandler, messageSystemHandlerDAO);
    }


    @Test
    void createMessageByChat() {
        assertDoesNotThrow(() -> {
            Mockito.when(messageSystemHandlerDAO.createMessageByChat(
                           Mockito.any(Message.class)
                   ))
                   .thenReturn(true);

            reflectInitMockFactoryForService(messageSystemHandlerDAO);

            var result = messageSystemHandler.createMessageByChat(messageDto);

            assertTrue(result);
        }, "Creating the message wasn't successful!");
    }

    @Test
    void readListMessageByChats() {
        assertDoesNotThrow(() -> {
            Mockito.when(messageSystemHandlerDAO.readListMessageByChats(
                           Mockito.anyList()
                   ))
                 .thenReturn(List.of(Mapper.convertMessageDTOIntoMessage(messageDto)));

            reflectInitMockFactoryForService(messageSystemHandlerDAO);

            var result = messageSystemHandler.readListMessageByChats(List.of(chatDtoGroup));

            if (result.isEmpty()) fail();

            assertEquals(List.of(messageDto), result);
        }, "Reading the message wasn't successful!");
    }
}