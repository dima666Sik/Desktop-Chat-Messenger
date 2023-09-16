package ua.desktop.chat.messenger.auth.domain.impl;

import org.junit.jupiter.api.*;
import ua.desktop.chat.messenger.dto.UserDTO;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceImplTest {

    private static AuthServiceImpl authService;
    private static UserDTO userDTO;

    @BeforeAll
    static void setUp() {
        authService = new AuthServiceImpl();
        userDTO = new UserDTO(6L, "devil", "devil", "devil");
    }

    @Test
    @Order(1)
    void registration() {
        Assertions.assertTrue(authService.registration(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword()));
    }

    @Test
    @Order(2)
    void authorization() {
        Optional<UserDTO> userDTOResult = authService.authorization(userDTO.getEmail(), userDTO.getPassword());
        if (userDTOResult.isEmpty()) fail();
        userDTO.setPassword(userDTOResult.get().getPassword());
        Assertions.assertEquals(userDTO, userDTOResult.get());
    }
}