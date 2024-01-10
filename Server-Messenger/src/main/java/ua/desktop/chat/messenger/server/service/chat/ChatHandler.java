package ua.desktop.chat.messenger.server.service.chat;

import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.desktop.chat.messenger.constant.ChatConstant;
import ua.desktop.chat.messenger.constant.MessageConstant;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.MessageDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;
import ua.desktop.chat.messenger.domain.env.TypeMessage;
import ua.desktop.chat.messenger.parser.ParserJSON;
import ua.desktop.chat.messenger.server.service.exception.UndefinedChatException;
import ua.desktop.chat.messenger.server.service.ClientHandler;
import ua.desktop.chat.messenger.server.service.ConnectionHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChatHandler {
    private static final Logger logger = LogManager.getLogger(ChatHandler.class);
    private final ClientHandler clientHandler;
    private final ConnectionHandler connectionHandler;

    public ChatHandler(ClientHandler clientHandler, ConnectionHandler connectionHandler) {
        this.clientHandler = clientHandler;
        this.connectionHandler = connectionHandler;
    }

    public void processInputChoice() throws IOException {
        String userInput;
        while ((userInput = clientHandler.getSocketInputReader().readLine()) != null) {
            switch (userInput) {
                case ChatConstant.MESSAGE_COMMAND:
                    clientHandler
                            .getMessageManager()
                            .processUserMessage();
                    break;
                case ChatConstant.UPDATE_GROUP_COMMAND:
                    clientHandler
                            .getChatManager()
                            .informAllClientsUserNameList();
                    break;
                case ChatConstant.EXIT_COMMAND:
                    clientHandler
                            .getProcessExitUser()
                            .processExitCommand();
                    break;
            }
        }
    }

    public boolean checkCountCompanion(UserDTO user, MessageDTO message, ClientHandler sender) {
        if (connectionHandler.getClientHandlers().size() <= 1) {
            message.setMessage(MessageConstant.MESSAGE_NOT_SENT);
            sender
                    .getMessageManager()
                    .sendMessage(ParserJSON.convertObjectToString(message, TypeMessage.MESSAGE_OBJECT));

            logger.info("{} this message for once user in chat: {}", MessageConstant.MESSAGE_NOT_SENT, user.getUsername());
            connectionHandler
                    .getServerHandlerGUI()
                    .updateChat(MessageConstant.MESSAGE_NOT_SENT + " this message for once user in chat: " + user.getUsername());
            return false;
        }
        return true;
    }

    public void sendUserNameList(Multimap<String, ChatDTO> ul) {
        StringBuilder userListResponse = new StringBuilder();
        clientHandler.getSocketOutputWriter().println(ChatConstant.USERS_COMMAND);

        for (Map.Entry<String, ChatDTO> entry : ul.entries()) {
            String userChat = entry.getKey();
            ChatDTO chatDTO = entry.getValue();

            // Add chats with current names into db if them not exist there
            userListResponse.append(userChat)
                    .append(":").append(chatDTO.getTypeChat())
                    .append(":").append(chatDTO.getUser().getUsername()).append(",");

            createChatsIfNotExist(userChat, chatDTO, clientHandler.getInitializeUser().getUserDTO());
        }
        clientHandler.getSocketOutputWriter().println(userListResponse);
    }

    public void informAllClientsUserNameList() {
        Multimap<String, ChatDTO> userNameAndChatInfo = connectionHandler.getUserNameAndChatInfo();
        userNameAndChatInfo.clear();
        fillChatNameList();
        fillChatNameListGroup();

        for (Map.Entry<String, ClientHandler> key : connectionHandler.getClientHandlers().entrySet()) {
            ClientHandler client = key.getValue();
            client.getChatManager().sendUserNameList(userNameAndChatInfo);
        }
    }

    private void fillChatNameList() {
        Multimap<String, ChatDTO> userNameAndChatInfo = connectionHandler.getUserNameAndChatInfo();
        for (Map.Entry<String, ClientHandler> key : connectionHandler.getClientHandlers().entrySet()) {
            ClientHandler client = key.getValue();
            userNameAndChatInfo.put(ChatConstant.GLOBAL_TYPE, new ChatDTO(TypeChat.GLOBAL, null, client.getInitializeUser().getUserDTO()));
            userNameAndChatInfo.put(client.getInitializeUser().getUsername(),
                    new ChatDTO(TypeChat.PRIVATE, client.getInitializeUser().getUserDTO().getId(),
                            client.getInitializeUser().getUserDTO()));
        }
    }

    private void fillChatNameListGroup() {
        Multimap<String, ChatDTO> userNameAndChatInfo = connectionHandler.getUserNameAndChatInfo();
        for (Map.Entry<String, ClientHandler> key : connectionHandler.getClientHandlers().entrySet()) {
            ClientHandler client = key.getValue();
            // read chats with a type group!
            List<ChatDTO> chatList = connectionHandler
                    .getChatSystemMessaging()
                    .readChatsByType(TypeChat.GROUP, client.getInitializeUser().getUserDTO().getId());
            if (chatList.isEmpty()) return;
            for (ChatDTO chat : chatList) {
                userNameAndChatInfo.put(chat.getNameChat(), new ChatDTO(chat.getTypeChat(), null, chat.getUser()));
            }
        }
    }

    public void createChatsIfNotExist(String userChat, ChatDTO chatDTO, UserDTO currentUserDTO) {
        new Thread(() -> {
            synchronized (this) {
                if (!connectionHandler
                        .getChatSystemMessaging()
                        .isExistChatByUser(userChat, currentUserDTO.getId())) {
                    if (chatDTO.getTypeChat() == TypeChat.GLOBAL) {
                        connectionHandler
                                .getChatSystemMessaging()
                                .createChatByUser(TypeChat.GLOBAL.name(), TypeChat.GLOBAL, currentUserDTO, chatDTO.getUserCompanionId());
                    } else if (chatDTO.getTypeChat() == TypeChat.PRIVATE
                            && (!userChat.equals(currentUserDTO.getUsername()))) {
                        connectionHandler
                                .getChatSystemMessaging()
                                .createChatByUser(userChat, TypeChat.PRIVATE, currentUserDTO, chatDTO.getUserCompanionId());
                    }
                }
            }
        }).start();
    }
}
