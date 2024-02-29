package ua.desktop.chat.messenger.client.ui.chat;

import ua.desktop.chat.messenger.client.service.Client;
import ua.desktop.chat.messenger.core.service.ChatSystemHandling;
import ua.desktop.chat.messenger.dao.exceptions.OpenSessionException;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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
            List<ChatDTO> listChats = null;
            try {
                listChats = chatSystemHandling.readListChatsByChatName(textFieldNameChat.getText());
                if (listChats.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Chat is not exist for anyone user!",
                            "Try again",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    chatSystemHandling.createChatByUser(listChats.get(0)
                                                                 .getNameChat(),
                            TypeChat.GROUP, client.getUser(), null);
                }
                client.getMessageManager()
                      .getChatManager()
                      .sendUpdateGroupIntoList();
            } catch (OpenSessionException ex) {
                JOptionPane.showMessageDialog(this,
                        "The opening session failed!",
                        "Try again",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
        setVisible(true);
    }
}
