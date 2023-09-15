package ua.desktop.chat.messenger.ui.chat;

import ua.desktop.chat.messenger.domain.ifaces.ChatSystemHandling;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InviteIntoChatGUI extends JDialog {
    private JPanel panelInviteIntoChat;
    private JTextField textFieldNameChat;
    private JButton cancelButton;
    private JButton confirmButton;
    private final ChatSystemHandling chatSystemHandling;
    private final User user;

    public InviteIntoChatGUI(ChatSystemHandling chatSystemHandling, User user) {
        this.chatSystemHandling = chatSystemHandling;
        this.user = user;
    }

    public void startGUI() {
        setContentPane(panelInviteIntoChat);
        setMinimumSize(new Dimension(400, 200));

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        confirmButton.addActionListener(e -> {
            dispose();
            List<Chat> listChats = chatSystemHandling.readListChatsByChatName(textFieldNameChat.getText());
            if (!listChats.isEmpty()) {
                chatSystemHandling.createChatByUser(listChats.get(0).getNameChat(),
                        TypeChat.GROUP, user, null);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Chat is not exist for anyone user!",
                        "Try again",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
        setVisible(true);
    }
}
