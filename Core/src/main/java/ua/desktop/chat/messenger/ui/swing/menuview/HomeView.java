package ua.desktop.chat.messenger.ui.swing.menuview;

import ua.desktop.chat.messenger.ui.swing.auth.AuthorizationView;
import ua.desktop.chat.messenger.ui.swing.auth.RegistrationView;

import javax.swing.*;
import java.awt.*;

public class HomeView extends JDialog {
    private JPanel panelHomeView;
    private JButton auth;
    private JButton register;
    private JButton exit;

    public HomeView() {
        setUndecorated(true);
        setContentPane(panelHomeView);
        setMinimumSize(new Dimension(520, 300));
        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        exit.addActionListener(e -> dispose());
        auth.addActionListener(e -> {
            dispose();
            new AuthorizationView();
        });
        register.addActionListener(e -> {
            dispose();
            new RegistrationView();
        });
        setVisible(true);
    }
}
