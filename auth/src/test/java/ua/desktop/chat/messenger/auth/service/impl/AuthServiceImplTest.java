package ua.desktop.chat.messenger.auth.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.desktop.chat.messenger.auth.dao.mysql.UserDAO;
import ua.desktop.chat.messenger.auth.service.AuthService;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.model.User;
import ua.desktop.chat.messenger.domain.util.Mapper;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    private static AuthService authService;
    private static UserDTO userDTO;
    private static UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = Mockito.mock(UserDAO.class);
        authService = new AuthServiceImpl();
        userDTO = new UserDTO(1L, "devil1", "devil", "devil");
    }

    void reflectInitMockFactoryForService(UserDAO userDAO) throws NoSuchFieldException, IllegalAccessException {
        var authServiceClass = authService.getClass();
        var fieldUserDao = authServiceClass.getDeclaredField("userDAO");
        fieldUserDao.setAccessible(true);
        fieldUserDao.set(authService, userDAO);
    }

    @Test
    void registrationUserSuccessful() throws OpenSessionException, NoSuchFieldException, IllegalAccessException {
        Mockito
                .when(userDAO.createUser(Mockito.any(User.class)))
                .thenReturn(true);
        reflectInitMockFactoryForService(userDAO);
        Assertions.assertTrue(authService.registration(userDTO.getUsername(), userDTO.getEmail(), userDTO.getPassword()));
    }

    @Test
    void authorizationUserSuccessful() throws OpenSessionException, NoSuchFieldException, IllegalAccessException {
        var optionalUser = Optional.of(Mapper.convertUserDTOIntoUser(userDTO));
        var optionalUserDTO = Optional.of(userDTO);
        Mockito
                .when(userDAO.findUserByEmailAndPassword(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(optionalUser);
        reflectInitMockFactoryForService(userDAO);
        Assertions.assertEquals(optionalUserDTO, authService.authorization(userDTO.getEmail(), userDTO.getPassword()));
    }
}