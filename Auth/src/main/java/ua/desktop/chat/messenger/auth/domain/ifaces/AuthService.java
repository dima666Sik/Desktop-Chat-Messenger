package ua.desktop.chat.messenger.auth.domain.ifaces;

import ua.desktop.chat.messenger.models.User;

public interface AuthService {
    boolean registration(final String userName, final String email, final String password);
    User authorization(final String email, final String password);

}
