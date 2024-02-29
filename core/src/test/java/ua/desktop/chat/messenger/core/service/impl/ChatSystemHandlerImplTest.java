package ua.desktop.chat.messenger.core.service.impl;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import ua.desktop.chat.messenger.core.dao.mysql.ChatSystemHandlerDAO;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.model.User;
import ua.desktop.chat.messenger.domain.util.Mapper;
import ua.desktop.chat.messenger.encryption.Encryption;
import ua.desktop.chat.messenger.exception.EncryptionException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ChatSystemHandlerImplTest {
    private static ChatSystemHandling chatSystemHandling;
    private static ChatSystemHandlerDAO chatSystemHandlerDAO;
    private static UserDTO userDto;
    private static ChatDTO chatDtoGroup;

    @BeforeEach
    void setupUp() throws EncryptionException {
        chatSystemHandling = new ChatSystemHandlerImpl();
        chatSystemHandlerDAO = Mockito.mock(ChatSystemHandlerDAO.class);

        userDto = new UserDTO(1L, "devil", "devil", Encryption.encryptionSHA3256("devil"));

        chatDtoGroup = new ChatDTO(7L, "Test Termopasta Grizli", TypeChat.GROUP, userDto, null);

    }

    void reflectInitMockFactoryForService(ChatSystemHandlerDAO chatSystemHandlerDAO)
            throws NoSuchFieldException, IllegalAccessException {
        var coreServiceClass = chatSystemHandling.getClass();
        var fieldUserDao = coreServiceClass.getDeclaredField("chatSystemMessageDAO");
        fieldUserDao.setAccessible(true);
        fieldUserDao.set(chatSystemHandling, chatSystemHandlerDAO);
    }

    @Test
    void createChatByUser() {
        Assertions.assertDoesNotThrow(() -> {
            Mockito.when(chatSystemHandlerDAO.createChatByUser(
                           Mockito.anyString(),
                           Mockito.any(TypeChat.class),
                           Mockito.any(User.class),
                           Mockito.any()
                   ))
                   .thenReturn(true);

            reflectInitMockFactoryForService(chatSystemHandlerDAO);

            boolean result = chatSystemHandling.createChatByUser(chatDtoGroup.getNameChat(),
                    chatDtoGroup.getTypeChat(), chatDtoGroup.getUser(), chatDtoGroup.getUserCompanionId());

            assertTrue(result);

        }, "Creating the chat wasn't successful!");
    }

    @Test
    void isExistChatByUser() {
        Assertions.assertDoesNotThrow(() -> {
            Mockito.when(chatSystemHandlerDAO.findChatByChatNameAndUserId(
                           Mockito.anyString(),
                           Mockito.any()
                   ))
                   .thenReturn(Optional.of(Mapper.convertChatDTOIntoChat(chatDtoGroup)));

            reflectInitMockFactoryForService(chatSystemHandlerDAO);

            boolean result = chatSystemHandling.isExistChatByUser(chatDtoGroup.getNameChat(),
                    chatDtoGroup.getUser()
                                .getId());

            assertTrue(result);

        }, "Creating the chat wasn't successful!");
    }

    @Test
    void readListChatsByUser() {
        Assertions.assertDoesNotThrow(() -> {
            Mockito.when(chatSystemHandlerDAO.readListChatsByUser(
                           Mockito.any(User.class)
                   ))
                   .thenReturn(List.of(Mapper.convertChatDTOIntoChat(chatDtoGroup)));

            reflectInitMockFactoryForService(chatSystemHandlerDAO);

            var result = chatSystemHandling.readListChatsByUser(chatDtoGroup.getUser());

            if (result.isEmpty()) fail();

            assertEquals(List.of(chatDtoGroup), result);
        }, "Reading the chat wasn't successful!");
    }

    @Test
    void readListChatsByChatName() {
        Assertions.assertDoesNotThrow(() -> {
            Mockito.when(chatSystemHandlerDAO.readListChatsByChatName(
                           Mockito.anyString()
                   ))
                   .thenReturn(List.of(Mapper.convertChatDTOIntoChat(chatDtoGroup)));

            reflectInitMockFactoryForService(chatSystemHandlerDAO);

            var result = chatSystemHandling.readListChatsByChatName(chatDtoGroup.getNameChat());

            if (result.isEmpty()) fail();

            assertEquals(List.of(chatDtoGroup), result);
        }, "Reading the chat wasn't successful!");
    }

    @Test
    void readChat() {
        Assertions.assertDoesNotThrow(() -> {
            Mockito.when(chatSystemHandlerDAO.findChatByChatNameAndUserId(
                           Mockito.anyString(),
                           Mockito.any()
                   ))
                   .thenReturn(Optional.of(Mapper.convertChatDTOIntoChat(chatDtoGroup)));

            reflectInitMockFactoryForService(chatSystemHandlerDAO);

            var result = chatSystemHandling.readChat(chatDtoGroup.getNameChat(),
                    chatDtoGroup.getUser()
                                .getId());

            if (result.isEmpty()) fail();

            assertEquals(chatDtoGroup, result.get());
        }, "Reading the chat wasn't successful!");
    }

    @Test
    void readChatCompanion() {
        Assertions.assertDoesNotThrow(() -> {
            Mockito.when(chatSystemHandlerDAO.readChatCompanion(
                           Mockito.any()
                   ))
                   .thenReturn(Optional.of(Mapper.convertChatDTOIntoChat(chatDtoGroup)));

            reflectInitMockFactoryForService(chatSystemHandlerDAO);

            var result = chatSystemHandling.readChatCompanion(new ChatDTO("tempChat", TypeChat.PRIVATE, new UserDTO()));

            if (result.isEmpty()) fail();

            assertEquals(chatDtoGroup, result.get());
        }, "Reading the chat wasn't successful!");
    }

    @Test
    void readChatsByType() {
        Assertions.assertDoesNotThrow(() -> {
            Mockito.when(chatSystemHandlerDAO.readChatsByType(
                           Mockito.any(TypeChat.class),
                           Mockito.any()
                   ))
                   .thenReturn(List.of(Mapper.convertChatDTOIntoChat(chatDtoGroup)));

            reflectInitMockFactoryForService(chatSystemHandlerDAO);

            var result = chatSystemHandling.readChatsByType(chatDtoGroup.getTypeChat(), userDto.getId());

            if (result.isEmpty()) fail();

            assertEquals(List.of(chatDtoGroup), result);
        }, "Reading the chat wasn't successful!");
    }

}