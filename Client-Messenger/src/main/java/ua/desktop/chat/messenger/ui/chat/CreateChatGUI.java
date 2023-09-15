package ua.desktop.chat.messenger.ui.chat;

import ua.desktop.chat.messenger.domain.ifaces.ChatSystemHandling;
import ua.desktop.chat.messenger.domain.impl.ChatSystemHandlerImpl;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.models.User;

import javax.swing.*;
import java.awt.*;

public class CreateChatGUI extends JDialog {
    private JTextField textFieldNameChat;
    private JButton confirmButton;
    private JButton cancelButton;
    private JComboBox comboBoxTypeChat;
    private JPanel panelCreateChat;
    private final ChatSystemHandling chatSystemHandling;
    private final UserDTO user;

    public CreateChatGUI(ChatSystemHandling chatSystemHandling, UserDTO user) {
        this.chatSystemHandling = chatSystemHandling;
        this.user = user;
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
            chatSystemHandling.createChatByUser(textFieldNameChat.getText(), typeChatChoose, user, null);
        });

        cancelButton.addActionListener(e -> dispose());
        setVisible(true);
    }
}
