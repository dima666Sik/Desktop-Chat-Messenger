package ua.desktop.chat.messenger.parser;

import org.junit.jupiter.api.*;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeMessage;

class ParserJSONTest {
    private static UserDTO userDTO;
    private static String userDTOAsStr;

    @BeforeAll
    static void setUp() {
        userDTO = new UserDTO(6L, "devil", "devil", "devil");
        userDTOAsStr = "{\"id\":6,\"username\":\"devil\",\"email\":\"devil\",\"password\":\"devil\",\"type\":\"USER_OBJECT\"}";
    }

    @Test
    void convertObjectToString() {
        Assertions.assertEquals(userDTOAsStr, ParserJSON.convertObjectToString(userDTO, TypeMessage.USER_OBJECT));
    }

    @Test
    void convertStringToObject() {
        Assertions.assertEquals(userDTO, ParserJSON.convertStringToObject(userDTOAsStr));
    }
}