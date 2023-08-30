package ua.desktop.chat.messenger.ui.swing;

import ua.desktop.chat.messenger.domain.client.ChatClientManager;
import ua.desktop.chat.messenger.domain.ifaces.ClientProvider;
import ua.desktop.chat.messenger.domain.ifaces.ServerProvider;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemMessaging;
import ua.desktop.chat.messenger.domain.impl.ChatSystemMessageImpl;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChatMessenger extends JDialog {
    private JTextArea textArea;
    private JTextField textFieldMessage;
    private JButton sendMessage;
    private JLabel jLabelUserName;
    private JPanel panelChat;
    private JLabel nameChat;
    private JList<String> jListChats;
    private List<Chat> listChats;
    private JButton createChatButton;
    private JButton inviteIntoCurrentChatButton;
    private final DefaultListModel<String> model;
    private final User user;

    private ClientProvider clientProvider;

    private ServerProvider serverProvider;

    public ChatMessenger(User user) {
        this.user = user;
        setContentPane(panelChat);
        setMinimumSize(new Dimension(580, 400));

        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        jLabelUserName.setText(user.getUsername());
        nameChat.setText("Nope choose chat!");
        model = new DefaultListModel<>();

        addDefaultWorldChat();
        initializationListChats();

        checkClickChoiceChat();

        createChatButton.addActionListener(e -> {
            CreateChat createChat = new CreateChat(user);
            addIntoJListChats(createChat.getChat().getNameChat(), createChat.getChat().getTypeChat());
            listChats.add(createChat.getChat());
        });

        setVisible(true);

    }

    private void initializationListChats() {
        listChats = getListChats();
        for (Chat chat : listChats) {
            addIntoJListChats(chat.getNameChat(), chat.getTypeChat());
        }
    }

    private void addDefaultWorldChat() {
        String nameChat = "Global chat";
        ChatSystemMessaging chatSystemMessaging = new ChatSystemMessageImpl();
        if (!chatSystemMessaging.isExistChatByUser(nameChat, user)) {
            if (chatSystemMessaging.createChatByUser(nameChat, TypeChat.GLOBAL, user) == null) {
                JOptionPane.showMessageDialog(this,
                        "Chat '".concat(nameChat).concat("' was not added to db!!!"),
                        "Try again",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private List<Chat> getListChats() {
        ChatSystemMessaging chatSystemMessaging = new ChatSystemMessageImpl();
        List<Chat> chatList = chatSystemMessaging.readListChatsByUser(user);
        if (chatList == null) {
            JOptionPane.showMessageDialog(this,
                    "Chats was not found in db for '"
                            .concat(user.getUsername())
                            .concat("' !!!"),
                    "Try again",
                    JOptionPane.WARNING_MESSAGE);
        }
        return chatList;
    }

    private void addIntoJListChats(String nameChat, TypeChat typeChat) {
        model.addElement("("
                .concat(typeChat.name())
                .concat(") ")
                .concat(nameChat));
        jListChats.setModel(model);
    }

    private void checkClickChoiceChat() {
        // Add a ListSelectionListener to the JList
        jListChats.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // Get the selected value from the JList
                int selectedChatIndex = jListChats.getSelectedIndex();
                nameChat.setText(jListChats.getSelectedValue());
                // Do something with the selected value
                System.out.println("Selected chat: " + selectedChatIndex);

                new Thread(() -> {
                    Chat chat = new Chat(listChats.get(selectedChatIndex).getNameChat(),
                            listChats.get(selectedChatIndex).getTypeChat(),
                            user);

                    clientProvider = ChatClientManager.getInstance(chat);
                    clientProvider.listenMessageFromClients();

                    sendMessage.addActionListener(e1 -> {
                        String messageStr = textFieldMessage.getText();
                        if (messageStr.isEmpty()) {
                            JOptionPane.showMessageDialog(this,
                                    "Your message is empty!!!",
                                    "Try again",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        Message message = new Message(messageStr, chat);
                        clientProvider.sendMessage(message);
                    });
                }).start();
            }
        });
    }
}
