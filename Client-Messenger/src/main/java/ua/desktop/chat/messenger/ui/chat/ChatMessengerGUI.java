package ua.desktop.chat.messenger.ui.chat;

import ua.desktop.chat.messenger.domain.Client;
import ua.desktop.chat.messenger.dto.ChatDTO;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.env.TypeChat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatMessengerGUI extends JDialog {
    private static final String DEFAULT_NAME_CHAT = "You not choose chat!";
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
        SwingUtilities.invokeLater(() -> {
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
        });
    }

    public void updateChat(final String serverMSG) {
        SwingUtilities.invokeLater(() -> {
            try {
                text.append(serverMSG).append(" \n");
                textArea.append(text.toString());
                clearText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private List<MessageDTO> sortListMessageForDate(List<MessageDTO> messageDTOs) {
        messageDTOs.sort(Comparator.comparing(MessageDTO::getLocalDateTime));
        return messageDTOs;
    }

    public void prePrinterMessagesInChatForUser(List<MessageDTO> messageDTOs) {
        SwingUtilities.invokeLater(() -> {
            textArea.setText("");
            clearText();
            try {
//                Optional<String> companionName =
//                        messageDTOs.stream()
//                                .filter(messageDTO -> !messageDTO.getChat().getNameChat().equals(client.getUser().getUsername()))
//                                .map(messageDTO -> messageDTO.getChat().getNameChat())
//                                .findFirst();
                for (MessageDTO messageDTO : messageDTOs) {
                    String messageText = null;
                    String companionName = messageDTO.getChat().getUser().getUsername();
                    if (messageDTO.getChat().getTypeChat() == TypeChat.PRIVATE) {
                        if (messageDTO.getChat().getNameChat().equals(client.getUser().getUsername())) {
                            messageText = buildMessageText(messageDTO, companionName, client.getUser().getUsername(), true);
                        } else {
                            messageText = buildMessageText(messageDTO, messageDTO.getChat().getNameChat(), client.getUser().getUsername(), false);
                        }
                    } else {
                        if (messageDTO.getChat().getUser().getUsername().equals(client.getUser().getUsername())) {
                            messageText = buildMessageText(messageDTO, companionName, client.getUser().getUsername(), true);
                        } else {
                            messageText = buildMessageText(messageDTO, messageDTO.getChat().getUser().getUsername(), client.getUser().getUsername(), false);
                        }
                    }
                    textArea.append(messageText);
                    clearText();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void clearText() {
        text.delete(0, text.length());
    }

    private void removeAllUsernames() {
        model.clear();
        jListChats.setModel(model);
    }

    private void checkClickChoiceChat() {
        // Add a ListSelectionListener to the JList
        jListChats.addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                Thread thread = new Thread(() -> {
                    synchronized (this) {
                        sendMessage.setEnabled(true);
                        // Get the selected value from the JList
                        if (jListChats.getSelectedValue() == null) nameChat.setText(textNameChat);
                        else nameChat.setText(jListChats.getSelectedValue());
                        textNameChat = nameChat.getText();
                        typeChat = nameChat.getText().equals(DEFAULT_NAME_CHAT) ? TypeChat.GLOBAL : nameChat.getText().equals(TypeChat.GLOBAL.name()) ? TypeChat.GLOBAL : TypeChat.PRIVATE;
                        ChatDTO chat = new ChatDTO(nameChat.getText(), typeChat, client.getUser());
                        List<MessageDTO> messageDTOList = client.getMessagesInChatForUser(chat);
                        prePrinterMessagesInChatForUser(sortListMessageForDate(messageDTOList));
                    }
                });

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public void startGUI() {
        initializeUIComponents();
        configureMainWindow();
        configureChatDisplay();
        configureSendMessageButton();
        configureWindowCloseListener();
        checkClickChoiceChat();

        setVisible(true);
    }

    private void initializeUIComponents() {
        setContentPane(panelChat);
        setMinimumSize(new Dimension(880, 400));
        setModal(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        nameChat.setText(DEFAULT_NAME_CHAT);
        model = new DefaultListModel<>();
        sendMessage.setEnabled(false);
    }

    private void configureMainWindow() {
        if (nameChat.getText().isEmpty()) {
            nameChat.setText(DEFAULT_NAME_CHAT);
        }

        textNameChat = nameChat.getText().equals(DEFAULT_NAME_CHAT) ? DEFAULT_NAME_CHAT : nameChat.getText();
        typeChat = nameChat.getText().equals(DEFAULT_NAME_CHAT) ? TypeChat.GLOBAL : TypeChat.PRIVATE;

        jLabelUserName.setText(client.getUser().getUsername());
    }

    private void configureChatDisplay() {
        textArea.append("Click on chat to show chat history!\n");
        textArea.append(String.format("Username [%s] | Host [%s] | Port [%s]\n",
                client.getUser().getUsername(), client.getHost(), client.getPortNumber()));
    }

    private void configureSendMessageButton() {
        sendMessage.addActionListener(e -> {
            String textMessage = textFieldMessage.getText();

            if (!textMessage.isEmpty()) {
                LocalDateTime localDateTime = LocalDateTime.now();
                ChatDTO chat = new ChatDTO(textNameChat, typeChat, client.getUser());
                MessageDTO message = new MessageDTO(textMessage, localDateTime, chat);

                client.sendMessage(message);

                String messageText = buildMessageText(message, textNameChat, client.getUser().getUsername(), true);
                textArea.append(messageText);
                textFieldMessage.setText("");
                clearText();
            }
        });
    }

    private String buildMessageText(MessageDTO messageDTO, String companionName, String clientUserName, boolean flagSender) {
        StringBuilder messageText = new StringBuilder("(");
        messageText.append(messageDTO.getLocalDateTime().format(formatter));

        if (typeChat == TypeChat.GLOBAL) {
            if (!flagSender) {
                messageText.append(")[GLOBAL] ").append(companionName).append(": ");
            } else {
                messageText.append(")[GLOBAL] ").append(clientUserName).append(": ");
            }
        } else {
            if (!flagSender) {
                messageText.append(")[PRIVATE] ").append(companionName).append(": ");
            } else
                messageText.append(")[TO [").append(companionName).append("]] ").append(clientUserName).append(": ");
        }

        messageText.append(messageDTO.getMessage()).append("\n");

        return messageText.toString();
    }

    private void configureWindowCloseListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Thread thread = new Thread(() -> {
                    if (client.getIsConnected()) {
                        client.setIsConnected(false);
                        client.getCommunicationHandler().setActive(false);
                        client.sendEXIT();
                        System.out.println("Exit from chat!");
                    }
                });

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
