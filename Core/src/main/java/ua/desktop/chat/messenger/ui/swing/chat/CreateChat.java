package ua.desktop.chat.messenger.ui.swing.chat;

import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemMessaging;
import ua.desktop.chat.messenger.domain.impl.ChatSystemMessageImpl;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.User;

import javax.swing.*;
import java.awt.*;

public class CreateChat extends JDialog {
    private JButton cancelButton;
    private JButton confirmButton;
    private JTextField textFieldNameChat;
    private JComboBox comboBoxListTypeChat;
    private JPanel panelCreateChat;
    private final User user;
    private Chat chat;

    public Chat getChat() {
        return chat;
    }

    public CreateChat(User user) {
        this.user = user;

        setContentPane(panelCreateChat);

        setMinimumSize(new Dimension(300, 200));

        confirmButton.addActionListener(e -> {
            addChat();
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setVisible(true);
    }

    private void addChat() {
        String nameChat = textFieldNameChat.getText();
        ChatSystemMessaging chatSystemMessaging = new ChatSystemMessageImpl();

        if (!chatSystemMessaging.isExistChatByUser(nameChat, user)) {
            chat = chatSystemMessaging.createChatByUser(nameChat, TypeChat.valueOf((String) comboBoxListTypeChat.getSelectedItem()), user);
            if (chat == null) {
                JOptionPane.showMessageDialog(this,
                        "Chat '".concat(nameChat).concat("' was not added to db!!!"),
                        "Try again",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

}
