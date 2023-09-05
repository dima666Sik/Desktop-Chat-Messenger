package ua.desktop.chat.messenger.ui.chat;

import ua.desktop.chat.messenger.domain.Client;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatMessenger extends JDialog {
    private static final String DEFAULT_NAME_CHAT = "Nope choose chat!";
    private JTextArea textArea;
    private JTextField textFieldMessage;
    private JButton sendMessage;
    private JLabel jLabelUserName;
    private JPanel panelChat;
    private JLabel nameChat;
    private JButton createChatButton;
    private JButton inviteIntoCurrentChatButton;
    private DefaultListModel<String> model;
    private Client client;
    private String text = "";
    private List<String> userList;
    private JList<String> jListChats;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public ChatMessenger(Client client) {
        this.client = client;
    }

    public void addUserList(final ArrayList<String> users) {
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
                text = serverMSG + " \n";
                textArea.append(text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    private void removeAllUsernames() {
        model.clear();
        model.addElement("GLOBAL");
        jListChats.setModel(model);
    }

    public void open() {
        setContentPane(panelChat);
        setMinimumSize(new Dimension(880, 400));

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        nameChat.setText(DEFAULT_NAME_CHAT);
        model = new DefaultListModel<>();

        textArea.append("Username " + "[" + client.getUser().getUsername() + "]" + " | Host " + "[" + client.getHost() + "]" + " | Port " + "[" + client.getPortNumber() + "]\n");

        jLabelUserName.setText(client.getUser().getUsername());

        checkClickChoiceChat();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(client.getIsConnected()){
                    client.setIsConnected(false);
                    client.getCommunicationHandler().setActive(false);
                    client.sendEXIT();
                    System.out.println("Exit from chat!");
                }
            }
        });


        createChatButton.addActionListener(e -> {
            //TODO
        });

        sendMessage.addActionListener(e -> {
            String textMessage = textFieldMessage.getText();

            if (nameChat.getText().equals(DEFAULT_NAME_CHAT)) nameChat.setText("GLOBAL");
            String textNameChat = nameChat.getText().equals("GLOBAL") ? TypeChat.GLOBAL.name() : nameChat.getText();
            TypeChat typeChat = nameChat.getText().equals("GLOBAL") ? TypeChat.GLOBAL : TypeChat.PRIVATE;

            if (!textMessage.isEmpty()) {
                //TODO
                LocalDateTime localDateTime = LocalDateTime.now();
                Chat chat = new Chat(textNameChat, typeChat, client.getUser());
                Message message = new Message(textMessage, localDateTime, chat);

                System.out.println("====================" + message.getMessage());
                client.sendMessage(message);

                System.out.println(textNameChat);
                if (textNameChat.equals("GLOBAL")) {
                    text = "(".concat(localDateTime.format(formatter))
                            .concat(")")
                            .concat("[GLOBAL] ")
                            .concat(client.getUser().getUsername())
                            .concat(": ")
                            .concat(textMessage)
                            .concat(" \n");
                } else {
                    text = "(".concat(localDateTime.format(formatter))
                            .concat(")")
                            .concat("[TO ")
                            .concat("[")
                            .concat(textNameChat)
                            .concat("]")
                            .concat("] ")
                            .concat(client.getUser().getUsername())
                            .concat(": ")
                            .concat(textMessage)
                            .concat(" \n");
                }
                textArea.append(text);
                textFieldMessage.setText("");
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
