package ua.desktop.chat.messenger.server.ui;

import ua.desktop.chat.messenger.server.service.ConnectionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerGUI extends JDialog {
    private JPanel panelServer;
    private JTextArea textAreaServer;
    private ConnectionHandler server;

    public void setServer(ConnectionHandler server) {
        this.server = server;
    }

    public void startGUI() {
        SwingUtilities.invokeLater(() -> {
            setContentPane(panelServer);
            setMinimumSize(new Dimension(500, 300));

            setModal(true);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            configureWindowCloseListener();
            setVisible(true);
        });
    }

    private void configureWindowCloseListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("help: " + server.getActive() + " - " + server.getClientHandlers().isEmpty()
                        + server + " " + server.getServerClosedHandler());
                if (server.getActive() && server.getClientHandlers().isEmpty()) {
                    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    System.out.println("-");
                    server.setActive(false);
                    System.out.println("-_");
                    server
                            .getServerClosedHandler()
                            .closeServerSocket();
                    System.out.println("-_-");
                }
            }
        });
    }

    public void updateChat(final String serverMSG) {
        SwingUtilities.invokeLater(() -> {
            try {
                textAreaServer.append(serverMSG.concat("\n"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
