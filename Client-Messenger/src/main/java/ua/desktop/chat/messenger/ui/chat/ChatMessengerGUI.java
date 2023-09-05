package ua.desktop.chat.messenger.ui.chat;

import ua.desktop.chat.messenger.domain.Client;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatMessengerGUI extends JDialog {
    private static final String DEFAULT_NAME_CHAT = "Nope choose chat!";
    private JTextArea textArea;
    private JTextField textFieldMessage;
    private JButton sendMessage;
    private JLabel jLabelUserName;
    private JPanel panelChat;
    private JLabel nameChat;
    private DefaultListModel<String> model;
    private final Client client;
    private final StringBuilder text = new StringBuilder();
    private JList<String> jListChats;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public ChatMessengerGUI(Client client) {
        this.client = client;
    }

    public void addUserList(final List<String> users) {
        new Thread(() -> SwingUtilities.invokeLater(() -> {
            try {
                removeAllUsernames();
                for (String user : users) {
                    if (!user.equals(client.getUser().getUsername())) {
                        model.addElement(user);
                        jListChats.setModel(model);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    public void updateChat(final String serverMSG) {
        new Thread(() -> SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("-------------" + serverMSG);
                text.append(serverMSG).append(" \n");
                textArea.append(text.toString());
                clearText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    public void clearText() {
        text.delete(0, text.length());
    }

    private void removeAllUsernames() {
        model.clear();
        model.addElement("GLOBAL");
        jListChats.setModel(model);
    }

    public void startGUI() {
        setContentPane(panelChat);
        setMinimumSize(new Dimension(880, 400));

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        nameChat.setText(DEFAULT_NAME_CHAT);
        model = new DefaultListModel<>();

        textArea.append("Username "
                .concat("[")
                .concat(client.getUser().getUsername())
                .concat("]")
                .concat(" | Host ")
                .concat("[")
                .concat(client.getHost())
                .concat("]")
                .concat(" | Port ")
                .concat("[")
                .concat(String.valueOf(client.getPortNumber()))
                .concat("]\n"));

        jLabelUserName.setText(client.getUser().getUsername());

        checkClickChoiceChat();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.getIsConnected()) {
                    client.setIsConnected(false);
                    client.getCommunicationHandler().setActive(false);
                    client.sendEXIT();
                    System.out.println("Exit from chat!");
                }
            }
        });

        sendMessage.addActionListener(e -> {
            String textMessage = textFieldMessage.getText();

            if (nameChat.getText().isEmpty() || nameChat.getText().equals(DEFAULT_NAME_CHAT))
                nameChat.setText("GLOBAL");
            String textNameChat = nameChat.getText().equals("GLOBAL") ? TypeChat.GLOBAL.name() : nameChat.getText();
            TypeChat typeChat = nameChat.getText().equals("GLOBAL") ? TypeChat.GLOBAL : TypeChat.PRIVATE;

            if (!textMessage.isEmpty()) {
                //TODO
                LocalDateTime localDateTime = LocalDateTime.now();
                ChatDTO chat = new ChatDTO(textNameChat, typeChat, client.getUser());
                MessageDTO message = new MessageDTO(textMessage, localDateTime, chat);

                System.out.println("====================" + message.getMessage());
                client.sendMessage(message);

                System.out.println(textNameChat);
                if (textNameChat.equals("GLOBAL")) {
                    text.append("(")
                            .append(localDateTime.format(formatter))
                            .append(")")
                            .append("[GLOBAL] ")
                            .append(client.getUser().getUsername())
                            .append(": ")
                            .append(textMessage)
                            .append(" \n");
                } else {
                    text.append("(")
                            .append(localDateTime.format(formatter))
                            .append(")")
                            .append("[TO ")
                            .append("[")
                            .append(textNameChat)
                            .append("]")
                            .append("] ")
                            .append(client.getUser().getUsername())
                            .append(": ")
                            .append(textMessage)
                            .append(" \n");
                }
                textArea.append(text.toString());
                textFieldMessage.setText("");
                clearText();
            }
        });

        setVisible(true);
    }

    private void checkClickChoiceChat() {
        // Add a ListSelectionListener to the JList
        jListChats.addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                // Get the selected value from the JList
                int selectedChatIndex = jListChats.getSelectedIndex();
                nameChat.setText(jListChats.getSelectedValue());
            }
        });
    }
}
