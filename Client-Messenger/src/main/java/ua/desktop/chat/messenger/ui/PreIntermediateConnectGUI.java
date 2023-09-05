package ua.desktop.chat.messenger.ui;

import ua.desktop.chat.messenger.domain.Client;
import ua.desktop.chat.messenger.models.User;

import javax.swing.*;
import java.awt.*;

public class PreIntermediateConnectGUI extends JDialog {
    public static final String HOST = "DESKTOP-OCNPFLI";
    public static final String PORT_NUMBER = "5000";
    private JTextField hostTextField;
    private JTextField portTextField;
    private JButton sendButton;
    private JPanel panelPreIntermediateConnect;
    private final Client client;

    public PreIntermediateConnectGUI(Client client) {
        this.client = client;
    }

    public void startGUI() {
        setUndecorated(true);
        setContentPane(panelPreIntermediateConnect);
        setMinimumSize(new Dimension(480, 300));

        hostTextField.setText(HOST);
        portTextField.setText(PORT_NUMBER);

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        sendButton.addActionListener(e -> {
            dispose();
            client.setHost(hostTextField.getText());
            client.setPortNumber(Integer.parseInt(portTextField.getText()));
            client.tryConnect();
        });
        setVisible(true);
    }
}
