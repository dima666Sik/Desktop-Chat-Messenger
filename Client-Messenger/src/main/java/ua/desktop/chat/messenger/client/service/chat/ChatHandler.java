package ua.desktop.chat.messenger.client.service.chat;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import ua.desktop.chat.messenger.client.exception.AddMessageException;
import ua.desktop.chat.messenger.client.exception.UndefinedChatException;
import ua.desktop.chat.messenger.client.service.CommunicationHandler;
import ua.desktop.chat.messenger.domain.dto.ChatDTO;
import ua.desktop.chat.messenger.domain.dto.UserDTO;
import ua.desktop.chat.messenger.domain.env.TypeChat;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.*;

public class ChatHandler {
    private final CommunicationHandler communicationHandler;
    private final UserDTO user;

    public ChatHandler(CommunicationHandler communicationHandler, UserDTO user) {
        this.communicationHandler = communicationHandler;
        this.user = user;
    }

    public void validateChatExistence(ChatDTO chatDTO) {
        if (!communicationHandler
                .getChatSystemMessaging()
                .isExistChatByUser(chatDTO.getNameChat(), user.getId())) {
            throw new AddMessageException("Message in chat was not added!");
        }
    }

    public List<ChatDTO> getChatList(ChatDTO chatDTO) {
        List<ChatDTO> chatORMList = new ArrayList<>();
        if (chatDTO.getTypeChat() == TypeChat.PRIVATE) {
            Optional<ChatDTO> chatOwner = communicationHandler
                    .getChatSystemMessaging()
                    .readChat(chatDTO.getNameChat(), user.getId());
            chatORMList.add(chatOwner.orElseThrow(() -> new UndefinedChatException("Chat is not found!")));

            Optional<ChatDTO> chatCompanion = communicationHandler
                    .getChatSystemMessaging()
                    .readChatCompanion(chatOwner.get());
            chatORMList.add(chatCompanion.orElseThrow(() -> new UndefinedChatException("Chat Companion is not found!")));
        } else {
            List<ChatDTO> chatDTOList = communicationHandler
                    .getChatSystemMessaging()
                    .readListChatsByChatName(chatDTO.getNameChat());
            if (chatDTOList.isEmpty()) throw new UndefinedChatException("Chats is not found!");
            chatORMList = chatDTOList;
        }
        return chatORMList;
    }

    public void sendEXIT() {
        communicationHandler
                .getSocketOutputWriter()
                .println("/EXIT");
    }

    public void sendUpdateGroupIntoList() {
        communicationHandler
                .getSocketOutputWriter()
                .println("/UPDATE GROUP INTO LIST");
    }

    public Multimap<String, ChatDTO> setUserList(String rspUserList) {
        Set<String> strChatAndType = new HashSet<>(Arrays.asList(rspUserList.split(",")));
        Multimap<String, ChatDTO> mapTypeChatMap = ArrayListMultimap.create();
        strChatAndType.forEach(element -> {
            List<String> stringList = Arrays.asList(element.split(":"));
            UserDTO userDTO = new UserDTO();
            System.out.println("stringList: "+stringList);
            userDTO.setUsername(stringList.get(2));
            mapTypeChatMap.put(stringList.get(0), new ChatDTO(TypeChat.valueOf(stringList.get(1)), null, userDTO));
        });
        return mapTypeChatMap;
    }
    public CommunicationHandler getCommunicationHandler() {
        return communicationHandler;
    }

    public UserDTO getUser() {
        return user;
    }

    public PrintWriter getSocketOutputWriter() {
        return communicationHandler.getSocketOutputWriter();
    }

    public BufferedReader getSocketInputReader() {
        return communicationHandler.getSocketInputReader();
    }
}
