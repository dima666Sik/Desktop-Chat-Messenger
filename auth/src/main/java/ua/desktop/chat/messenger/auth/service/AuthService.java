package ua.desktop.chat.messenger.auth.service;

import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.dto.UserDTO;

import java.util.Optional;

public interface AuthService {
    boolean registration(final String userName, final String email, final String password) throws OpenSessionException;
    Optional<UserDTO> authorization(final String email, final String password) throws OpenSessionException;
}
