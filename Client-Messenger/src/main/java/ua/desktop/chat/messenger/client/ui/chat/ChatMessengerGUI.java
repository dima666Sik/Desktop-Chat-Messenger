package ua.desktop.chat.messenger.client.ui.chat;

import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.client.exception.UndefinedChatException;
import ua.desktop.chat.messenger.dao.util.DBConnector;
import ua.desktop.chat.messenger.client.service.Client;
import ua.desktop.chat.messenger.core.service.impl.ChatSystemHandlerImpl;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ChatMessengerGUI extends JDialog {
    private static final Logger logger = LogManager.getLogger(ChatMessengerGUI.class.getName());
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
    private JButton createChatButton;
    private JButton inviteIntoChatButton;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private String textNameChat;
    private TypeChat typeChat;

    public ChatMessengerGUI(Client client) {
        this.client = client;
    }

    public void addChatList(final Multimap<String, ChatDTO> chats) {
        SwingUtilities.invokeLater(() -> {
            logger.info("Before updating {}", nameChat);
            removeAllUsernames();
            for (Map.Entry<String, ChatDTO> entry : chats.entries()) {
                if (entry.getValue().getTypeChat() == TypeChat.PRIVATE && !entry.getKey().equals(client.getUser().getUsername())
                        || (entry.getValue().getTypeChat() == TypeChat.GROUP && entry.getValue().getUser().getUsername().equals(client.getUser().getUsername()))
                        || (entry.getValue().getTypeChat() == TypeChat.GLOBAL && entry.getValue().getUser().getUsername().equals(client.getUser().getUsername()))) {
                    model.addElement(String.format("%s [%s]", entry.getKey(), entry.getValue().getTypeChat()));
                    jListChats.setModel(model);
                }
            }
            logger.info("After updating {}", nameChat);
        });
    }

    public void updateChat(final String serverMSG) {
        SwingUtilities.invokeLater(() -> {
            text.append(serverMSG).append(" \n");
            textArea.append(text.toString());
            clearText();
        });
    }

    public void updateChat(final MessageDTO serverMSG) {
        SwingUtilities.invokeLater(() -> {
            if (serverMSG.getChat().getUser().getUsername().equals(textNameChat) && serverMSG.getChat().getTypeChat() == TypeChat.PRIVATE
                    || serverMSG.getChat().getNameChat().equals(textNameChat)
                    && (!serverMSG.getChat().getUser().getUsername().equals(client.getUser().getUsername()))
                    && (serverMSG.getChat().getTypeChat() == TypeChat.GLOBAL
                    || serverMSG.getChat().getTypeChat() == TypeChat.GROUP)) {
                text.append(serverMSG.getMessage()).append(" \n");
                textArea.append(text.toString());
                clearText();
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

            StringBuilder messageBuilder = new StringBuilder();

            for (MessageDTO messageDTO : messageDTOs) {
                String messageText = buildMessageText(messageDTO);
                messageBuilder.append(messageText);
                clearText();
            }

            textArea.append(messageBuilder.toString());
        });
    }

    private String buildMessageText(MessageDTO messageDTO) {
        String companionName = messageDTO.getChat().getUser().getUsername();

        if (messageDTO.getChat().getTypeChat() == TypeChat.PRIVATE) {
            return buildPrivateChatMessageText(messageDTO, companionName);
        } else {
            return buildGroupChatMessageText(messageDTO, companionName);
        }
    }

    private String buildPrivateChatMessageText(MessageDTO messageDTO, String companionName) {
        String senderName = messageDTO.getChat().getUser().getUsername();
        boolean isCurrentUser = senderName.equals(client.getUser().getUsername());

        String chatName = isCurrentUser ? companionName : messageDTO.getChat().getNameChat();

        return buildMessageText(messageDTO, chatName, senderName, !isCurrentUser);
    }

    private String buildGroupChatMessageText(MessageDTO messageDTO, String companionName) {
        String senderName = messageDTO.getChat().getUser().getUsername();
        boolean isCurrentUser = senderName.equals(client.getUser().getUsername());

        String chatName = isCurrentUser ? companionName : senderName;

        return buildMessageText(messageDTO, chatName, senderName, !isCurrentUser);
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

                        Map<String, TypeChat> nameAndTypeChatMap = generateMapFromUIDataChatNameAndTypeChat(jListChats.getSelectedValue());
                        nameAndTypeChatMap.forEach((k, v) -> {
                            if (jListChats.getSelectedValue() == null) nameChat.setText(textNameChat);
                            else nameChat.setText(k);
                            textNameChat = k;
                            typeChat = v;
                            ChatDTO chat = new ChatDTO(k, typeChat, client.getUser());
                            List<MessageDTO> messageDTOList = client
                                    .getMessageManager()
                                    .getMessagesInChatForUser(chat);
                            prePrinterMessagesInChatForUser(sortListMessageForDate(messageDTOList));
                        });
                    }
                });

                thread.start();
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
        createChat();
        inviteIntoChat();
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
            nameChat.setText(textNameChat);
        }

        if (!nameChat.getText().equals(DEFAULT_NAME_CHAT)) {
            Map<String, TypeChat> nameAndTypeChatMap = generateMapFromUIDataChatNameAndTypeChat(nameChat.getText());
            nameAndTypeChatMap.forEach((k, v) -> {
                textNameChat = k;
                typeChat = v;
            });
        } else {
            textNameChat = DEFAULT_NAME_CHAT;
            typeChat = null;
        }
        jLabelUserName.setText(client.getUser().getUsername());
    }

    public Map<String, TypeChat> generateMapFromUIDataChatNameAndTypeChat(String nameChat) {
        if (nameChat != null) {
            List<String> stringList = Arrays.asList(nameChat.split(" "));
            Map<String, TypeChat> stringTypeChatMap = new HashMap<>();
            String originType = stringList.get(1).replace("[", "").replace("]", "");
            stringTypeChatMap.put(stringList.get(0), TypeChat.valueOf(originType));
            return stringTypeChatMap;
        }
        throw new UndefinedChatException("Empty name chat!");
    }

    private void configureChatDisplay() {
        textArea.append("Click on chat to show chat history!\n");
        textArea.append(String.format("Username [%s] | Host [%s] | Port [%s]%n",
                client.getUser().getUsername(), client.getHost(), client.getPortNumber()));
    }

    private void configureSendMessageButton() {
        sendMessage.addActionListener(e -> {
            String textMessage = textFieldMessage.getText();

            if (!textMessage.isEmpty()) {
                LocalDateTime localDateTime = LocalDateTime.now();
                ChatDTO chat = new ChatDTO(textNameChat, typeChat, client.getUser());
                MessageDTO message = new MessageDTO(textMessage, localDateTime, chat);

                client.getMessageManager().sendMessage(message);

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
        } else if (typeChat == TypeChat.GROUP) {
            if (!flagSender) {
                messageText.append(")[GROUP] ").append(companionName).append(": ");
            } else {
                messageText.append(")[GROUP] ").append(clientUserName).append(": ");
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
                        DBConnector.closeSessionFactory();
                        client.getMessageManager().getChatManager().sendEXIT();
                        logger.info("Exit from chat!");
                    }
                });

                thread.start();
            }
        });
    }

    private void createChat() {
        createChatButton.addActionListener(e -> {
            CreateChatGUI createChatGUI = new CreateChatGUI(new ChatSystemHandlerImpl(), client);
            createChatGUI.startGUI();
        });
    }

    private void inviteIntoChat() {
        inviteIntoChatButton.addActionListener(e -> {
            InviteIntoChatGUI inviteIntoChatGUI = new InviteIntoChatGUI(new ChatSystemHandlerImpl(), client);
            inviteIntoChatGUI.startGUI();
        });
    }
}
