package ua.desktop.chat.messenger.client.ui;

import ua.desktop.chat.messenger.prop.PropertiesFile;
import ua.desktop.chat.messenger.client.domain.Client;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class PreIntermediateConnectGUI extends JDialog {
    private static final String NAME_PROP_FILE = "client_connection.properties";
    private static final String PROP_VALUE_CLIENT_PORT = "client.connection.port";
    private static final String PROP_VALUE_CLIENT_HOST = "client.connection.host";
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
        setMinimumSize(new Dimension(440, 200));

        Properties properties = PropertiesFile.getProp(NAME_PROP_FILE);
        System.out.println("---"+properties.getProperty(PROP_VALUE_CLIENT_HOST));
        hostTextField.setText(properties.getProperty(PROP_VALUE_CLIENT_HOST));
        portTextField.setText(properties.getProperty(PROP_VALUE_CLIENT_PORT));

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
