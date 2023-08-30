package ua.desktop.chat.messenger.ui.swing.auth;

import ua.desktop.chat.messenger.domain.impl.auth.AuthServiceImpl;
import ua.desktop.chat.messenger.domain.ifaces.AuthService;
import ua.desktop.chat.messenger.models.User;
import ua.desktop.chat.messenger.ui.swing.ChatMessenger;
import ua.desktop.chat.messenger.ui.swing.menuview.HomeView;

import javax.swing.*;
import java.awt.*;

public class AuthorizationView extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton previousButton;
    private JButton registrationButton;
    private JButton confirmAuthorizationButton;
    private JPanel panelAuthorization;

    public AuthorizationView() {
        setUndecorated(true);
        setContentPane(panelAuthorization);
        setMinimumSize(new Dimension(480, 300));

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        previousButton.addActionListener(e -> {
            dispose();
            new HomeView();

        });

        confirmAuthorizationButton.addActionListener(e -> authorization());

        registrationButton.addActionListener(e -> {
            dispose();
            new RegistrationView();
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

        AuthService authService = new AuthServiceImpl();
        User user = authService.authorization(emailField.getText(), new String(passwordField.getPassword()));

        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "Authorization not successful!",
                    "Try again",
                    JOptionPane.WARNING_MESSAGE);

            clearFieldsForm();
            return;
        }

        dispose();
        new ChatMessenger(user);
    }

    private void clearFieldsForm() {
        emailField.setText("");
        passwordField.setText("");
    }
}
