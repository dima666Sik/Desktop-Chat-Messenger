package ua.desktop.chat.messenger.ui.swing.auth;

import ua.desktop.chat.messenger.domain.impl.auth.AuthServiceImpl;
import ua.desktop.chat.messenger.domain.ifaces.AuthService;
import ua.desktop.chat.messenger.ui.swing.menuview.HomeView;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class RegistrationView extends JDialog {
    private JPanel panelRegistration;
    private JTextField userName;
    private JTextField email;
    private JPasswordField password;
    private JButton previousButton;
    private JButton authorizationButton;
    private JButton confirmRegistrationButton;
    private JPasswordField confirmPassword;

    public RegistrationView() {
        setUndecorated(true);
        setContentPane(panelRegistration);
        setMinimumSize(new Dimension(520, 500));
        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        previousButton.addActionListener(e -> {
            dispose();
            new HomeView();
        });

        authorizationButton.addActionListener(e -> {
            dispose();
            new AuthorizationView();
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

        AuthService authService = new AuthServiceImpl();

        if (authService.registration(userName.getText(),
                email.getText(), new String(password.getPassword()))) {
            dispose();
            new AuthorizationView();
        } else {
            JOptionPane.showMessageDialog(this,
                    "User is not register! Please change email.",
                    "Try again",
                    JOptionPane.WARNING_MESSAGE);
            clearFieldsForm();
        }

    }

    private void clearFieldsForm() {
        userName.setText("");
        email.setText("");
        password.setText("");
        confirmPassword.setText("");
    }
}
