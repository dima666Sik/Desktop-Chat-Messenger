package ua.desktop.chat.messenger.auth.ui.swing.auth;

import ua.desktop.chat.messenger.auth.service.AuthService;
import ua.desktop.chat.messenger.auth.service.impl.AuthServiceImpl;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class RegistrationGUI extends JDialog {
    private JPanel panelRegistration;
    private JTextField userName;
    private JTextField email;
    private JPasswordField password;
    private JButton authorizationButton;
    private JButton confirmRegistrationButton;
    private JPasswordField confirmPassword;
    private final AuthService authService = new AuthServiceImpl();
    private final AuthorizationGUI authorizationGUI;

    public RegistrationGUI(AuthorizationGUI authorizationGUI) {
        this.authorizationGUI = authorizationGUI;
        authorizationGUI.setVisible(false);
    }

    public void startGUI() {
        setUndecorated(true);
        setContentPane(panelRegistration);
        setMinimumSize(new Dimension(520, 500));
        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        authorizationButton.addActionListener(e -> {
            dispose();
            authorizationGUI.setVisible(true);
        });

        confirmRegistrationButton.addActionListener(e -> confirmRegistration());

        setVisible(true);
    }

    private void confirmRegistration() {
        if (userName.getText().isEmpty() ||
                email.getText().isEmpty() ||
                password.getPassword().length == 0 ||
                confirmPassword.getPassword().length == 0
        ) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all fields...",
                    "Try again",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Arrays.equals(password.getPassword(), confirmPassword.getPassword())) {
            JOptionPane.showMessageDialog(this,
                    "Fields 'password' and 'confirm password' is not equals...",
                    "Try again",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.getPassword().length < 8) {
            JOptionPane.showMessageDialog(this,
                    "Password less 8 symbols.",
                    "Try again",
                    JOptionPane.WARNING_MESSAGE);
            return;

        }

        try {
            if (authService.registration(userName.getText(),
                    email.getText(), new String(password.getPassword()))) {
                dispose();
                authorizationGUI.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "User is not register! Please change email.",
                        "Try again",
                        JOptionPane.WARNING_MESSAGE);
                clearFieldsForm();
            }
        } catch (OpenSessionException e) {
            JOptionPane.showMessageDialog(this,
                    "The opening session failed!",
                    "Try again",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    private void clearFieldsForm() {
        userName.setText("");
        email.setText("");
        password.setText("");
        confirmPassword.setText("");
    }
}
