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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ChatMessengerGUI extends JDialog {
    private static final String DEFAULT_NAME_CHAT = "GLOBAL";
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
    private String textNameChat;
    private TypeChat typeChat;

    public ChatMessengerGUI(Client client) {
        this.client = client;
    }

    public void addUserList(final Set<String> users) {
        new Thread(() -> SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Before updating " + nameChat);
                removeAllUsernames();
                for (String user : users) {
                    if (!user.equals(client.getUser().getUsername())) {
                        model.addElement(user);
                        jListChats.setModel(model);
                    }
                }
                System.out.println("After updating " + nameChat);
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    public void updateChat(final String serverMSG) {
        new Thread(() -> SwingUtilities.invokeLater(() -> {
            try {
                text.append(serverMSG).append(" \n");
                textArea.append(text.toString());
                clearText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    private List<MessageDTO> sortListMessageForDate(List<MessageDTO> messageDTOs) {
        messageDTOs.sort(Comparator.comparing(MessageDTO::getLocalDateTime));
        return messageDTOs;
    }

    public void prePrinterMessagesInChatForUser(List<MessageDTO> messageDTOs) {
        new Thread(() -> SwingUtilities.invokeLater(() -> {
            textArea.setText("");
            clearText();
            try {
                for (MessageDTO messageDTO : messageDTOs) {
                    textArea.append(messageDTO.getMessage().concat("\n"));
                    clearText();
                }
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

        if (nameChat.getText().isEmpty()) nameChat.setText(DEFAULT_NAME_CHAT);

        textNameChat = nameChat.getText().equals(DEFAULT_NAME_CHAT) ? DEFAULT_NAME_CHAT : nameChat.getText();
        typeChat = nameChat.getText().equals(DEFAULT_NAME_CHAT) ? TypeChat.GLOBAL : TypeChat.PRIVATE;

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

            if (!textMessage.isEmpty()) {
                //TODO

                LocalDateTime localDateTime = LocalDateTime.now();
                ChatDTO chat = new ChatDTO(textNameChat, typeChat, client.getUser());
                MessageDTO message = new MessageDTO(textMessage, localDateTime, chat);

                client.sendMessage(message);

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
                if (jListChats.getSelectedValue() == null) nameChat.setText(textNameChat);
                else nameChat.setText(jListChats.getSelectedValue());

                textNameChat = nameChat.getText();
                typeChat = nameChat.getText().equals(DEFAULT_NAME_CHAT) ? TypeChat.GLOBAL : TypeChat.PRIVATE;
                ChatDTO chat = new ChatDTO(nameChat.getText(), typeChat, client.getUser());
                prePrinterMessagesInChatForUser(sortListMessageForDate(client.getMessagesInChatForUser(chat)));
            }
        });
    }
}
