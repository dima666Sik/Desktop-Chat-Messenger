package ua.desktop.chat.messenger.auth.service;

import ua.desktop.chat.messenger.domain.dto.UserDTO;

import java.util.Optional;

public interface AuthService {
    boolean registration(final String userName, final String email, final String password);
    Optional<UserDTO> authorization(final String email, final String password);
}
