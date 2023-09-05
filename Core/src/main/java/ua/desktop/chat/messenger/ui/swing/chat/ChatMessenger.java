package ua.desktop.chat.messenger.ui.swing.chat;

import ua.desktop.chat.messenger.domain.client.ChatClientManager;
import ua.desktop.chat.messenger.domain.client.ClientMessageListener;
import ua.desktop.chat.messenger.domain.ifaces.ClientProvider;
import ua.desktop.chat.messenger.domain.client.ClientMessageSender;
import ua.desktop.chat.messenger.env.TypeChat;
import ua.desktop.chat.messenger.domain.ifaces.ChatSystemMessaging;
import ua.desktop.chat.messenger.domain.impl.ChatSystemMessageImpl;
import ua.desktop.chat.messenger.models.Chat;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
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
//    private Thread threadChat;
    private Thread threadListenerMessage;
    private Thread threadSenderMessage;
    private ClientMessageListener runnableListenerMessage;
    private ClientMessageSender senderMessage;
    private Chat chat;

    public ChatMessenger(User user) {
        this.user = user;
        setContentPane(panelChat);
        setMinimumSize(new Dimension(880, 400));

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
            if (createChat.getChat() != null) {
                addIntoJListChats(createChat.getChat().getNameChat(), createChat.getChat().getTypeChat());
                listChats.add(createChat.getChat());
            }
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

                // close past active threads
                if (
//                        threadChat != null &&
                        threadListenerMessage != null) {
//                    System.out.println(threadChat.getName());
                    System.out.println(threadListenerMessage.getName());
                    clientProvider.closeClient();
                    threadListenerMessage.interrupt();
                    try {
                        threadListenerMessage.join();
                        System.out.println("threadListenerMessage join");
//                        threadChat.interrupt();
//                        threadChat.join();
//                        System.out.println("threadChat join");
                    } catch (InterruptedException e1) {
                        throw new RuntimeException(e1);
                    }
                }

                // create thread for chat
//                threadChat = new Thread(() -> {
                chat = new Chat(listChats.get(selectedChatIndex).getNameChat(),
                        listChats.get(selectedChatIndex).getTypeChat(),
                        user);

                clientProvider = ChatClientManager.getInstance(chat);

                System.out.println(clientProvider.getObjectInputStream());
                System.out.println(clientProvider.getObjectOutputStream());
                System.out.println(clientProvider.getSocket());

                welcomeClientIntoChat();

                senderMessage = new ClientMessageSender(clientProvider.getSocket(),
                        clientProvider.getObjectOutputStream());

                // create thread for listening message in current chat
                runnableListenerMessage = new ClientMessageListener(clientProvider.getSocket(),
                        clientProvider.getObjectInputStream(),
                        textArea);
                threadListenerMessage = new Thread(runnableListenerMessage);
                threadListenerMessage.start();

                sendMessage.addActionListener(e1 -> {
                    String messageStr = textFieldMessage.getText();
                    if (messageStr.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "Your message is empty!!!",
                                "Try again",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // create thread for send message in current chat
                    Message message = new Message(messageStr, chat);
                    senderMessage.sendClientMessage(message);
                });
//                });
//                threadChat.start();
            }
        });
    }

    private void welcomeClientIntoChat() {
        String welcomeStringMessage = "SERVER".concat(": ")
                .concat(user.getUsername()
                        .concat(" was added into world chat! Welcome!!!"))
                .concat("\tTime added into chat: ")
                .concat(String.valueOf(LocalDateTime.now()))
                .concat("\n");

        textArea.append(welcomeStringMessage);

        System.out.println("SERVER".concat(": ")
                .concat(user.getUsername()
                        .concat(" was added into world chat!"))
                .concat("\tTime added into chat: ")
                .concat(String.valueOf(LocalDateTime.now())));
    }
}
