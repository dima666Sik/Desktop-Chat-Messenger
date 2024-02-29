package ua.desktop.chat.messenger.auth.ui.swing.auth;

import ua.desktop.chat.messenger.auth.service.AuthService;
import ua.desktop.chat.messenger.auth.service.impl.AuthServiceImpl;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.dto.UserDTO;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class AuthorizationGUI extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton registrationButton;
    private JButton confirmAuthorizationButton;
    private JPanel panelAuthorization;
    private UserDTO user;
    private final AuthService authService = new AuthServiceImpl();

    public void startGUI() {
        setUndecorated(true);
        setContentPane(panelAuthorization);
        setMinimumSize(new Dimension(480, 300));

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        confirmAuthorizationButton.addActionListener(e -> authorization());

        registrationButton.addActionListener(e -> {
            RegistrationGUI registrationGUI = new RegistrationGUI(this);
            registrationGUI.startGUI();
        });
        setVisible(true);
    }

    private void authorization() {
        if (emailField.getText().isEmpty() ||
                passwordField.getPassword().length == 0
        ) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all fields...",
                    "Try again",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (passwordField.getPassword().length < 8) {
            JOptionPane.showMessageDialog(this,
                    "Password less 8 symbols.",
                    "Try again",
                    JOptionPane.WARNING_MESSAGE);
            return;

        }

        Optional<UserDTO> optionalUserDTO;
        try {
            optionalUserDTO = authService.authorization(emailField.getText(), new String(passwordField.getPassword()));
        } catch (OpenSessionException e) {
            JOptionPane.showMessageDialog(this,
                    "The opening session failed!",
                    "Try again",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (optionalUserDTO.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Authorization not successful!",
                    "Try again",
                    JOptionPane.WARNING_MESSAGE);

            clearFieldsForm();
            return;
        }

        user = optionalUserDTO.get();
        dispose();
    }

    private void clearFieldsForm() {
        emailField.setText("");
        passwordField.setText("");
    }

    public UserDTO getUser() {
        return user;
    }
}
