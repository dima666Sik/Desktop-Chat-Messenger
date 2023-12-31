package ua.desktop.chat.messenger.client.ui.chat;

import ua.desktop.chat.messenger.client.service.Client;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.domain.env.TypeChat;

import javax.swing.*;
import java.awt.*;

public class CreateChatGUI extends JDialog {
    private JTextField textFieldNameChat;
    private JButton confirmButton;
    private JButton cancelButton;
    private JComboBox comboBoxTypeChat;
    private JPanel panelCreateChat;
    private final ChatSystemHandling chatSystemHandling;
    private final Client client;

    public CreateChatGUI(ChatSystemHandling chatSystemHandling, Client client) {
        this.chatSystemHandling = chatSystemHandling;
        this.client = client;
    }

    public void startGUI() {
        setContentPane(panelCreateChat);
        setMinimumSize(new Dimension(400, 200));

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        confirmButton.addActionListener(e -> {
            dispose();
            TypeChat typeChatChoose = TypeChat.valueOf(String.valueOf(comboBoxTypeChat.getSelectedItem()));
            chatSystemHandling.createChatByUser(textFieldNameChat.getText(), typeChatChoose, client.getUser(), null);
            client.getMessageManager()
                    .getChatManager()
                    .sendUpdateGroupIntoList();
        });

        cancelButton.addActionListener(e -> dispose());
        setVisible(true);
    }
}
