package ua.desktop.chat.messenger.dao.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ReflectPermission;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class DBConnectorTest {

    private static SessionFactory sessionFactory;

    @BeforeEach
    void setUp() {
        sessionFactory = mock(SessionFactory.class);
    }

    void reflectInitMockFactoryForService(SessionFactory sessionFactory) throws NoSuchFieldException, IllegalAccessException {
        var dbConnClass = DBConnector.class;
        var fieldUserDao = dbConnClass.getDeclaredField("SESSION_FACTORY");
        fieldUserDao.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(fieldUserDao, fieldUserDao.getModifiers() & ~Modifier.FINAL);

        fieldUserDao.set(dbConnClass, sessionFactory);
    }

    @Test
    void getSessionShouldSuccess() {
        assertDoesNotThrow(()->{
            var session = mock(Session.class);
            when(sessionFactory.openSession()).thenReturn(session);

            reflectInitMockFactoryForService(sessionFactory);
            // Act
            var actualSession = DBConnector.getSession();

            // Assert
            assertNotNull(actualSession);
            verify(sessionFactory, times(1)).openSession();
        },"Connecting wasn't successful!");
    }

    @Test
    void getSessionShouldException() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        when(sessionFactory.openSession()).thenThrow(new RuntimeException("Failed to open session"));

        reflectInitMockFactoryForService(sessionFactory);

        // Act and Assert
        assertThrows(OpenSessionException.class, DBConnector::getSession);
    }
}