package ua.desktop.chat.messenger.ui;

import ua.desktop.chat.messenger.domain.Client;
import ua.desktop.chat.messenger.models.User;

import javax.swing.*;
import java.awt.*;

public class PreIntermediateConnectUI extends JDialog {
    private JTextField hostTextField;
    private JTextField portTextField;
    private JTextField userNameTextField;
    private JButton sendButton;
    private JPanel panelPreIntermediateConnect;
    private JButton cancelButton;

    public PreIntermediateConnectUI(Client client) {
        setUndecorated(true);
        setContentPane(panelPreIntermediateConnect);
        setMinimumSize(new Dimension(480, 300));

        hostTextField.setText("DESKTOP-OCNPFLI");
        portTextField.setText("5000");

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cancelButton.addActionListener(e -> {
            dispose();
            client.setIsConnected(false);
            client.getCommunicationHandler().setActive(false);
        });

        sendButton.addActionListener(e -> {
            dispose();
            User user = new User(userNameTextField.getText(), "email", "password");
            client.setHost(hostTextField.getText());
            client.setPortNumber(Integer.parseInt(portTextField.getText()));
//            client.setUsername(userNameTextField.getText());
            client.setUser(user);
            client.tryConnect();
//            new RegistrationView();
        });
        setVisible(true);
    }
}
