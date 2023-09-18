package ua.desktop.chat.messenger.ui.chat;

import ua.desktop.chat.messenger.domain.Client;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemHandling;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InviteIntoChatGUI extends JDialog {
    private JPanel panelInviteIntoChat;
    private JTextField textFieldNameChat;
    private JButton cancelButton;
    private JButton confirmButton;
    private final ChatSystemHandling chatSystemHandling;
    private final Client client;

    public InviteIntoChatGUI(ChatSystemHandling chatSystemHandling, Client client) {
        this.chatSystemHandling = chatSystemHandling;
        this.client = client;
    }

    public void startGUI() {
        setContentPane(panelInviteIntoChat);
        setMinimumSize(new Dimension(400, 200));

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        confirmButton.addActionListener(e -> {
            dispose();
            Optional<List<ChatDTO>> listChats = chatSystemHandling.readListChatsByChatName(textFieldNameChat.getText());
            if (listChats.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Chat is not exist for anyone user!",
                        "Try again",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                chatSystemHandling.createChatByUser(listChats.get().get(0).getNameChat(),
                        TypeChat.GROUP, client.getUser(), null);
            }
            client.sendUpdateGroupIntoList();
        });

        cancelButton.addActionListener(e -> dispose());
        setVisible(true);
    }
}
