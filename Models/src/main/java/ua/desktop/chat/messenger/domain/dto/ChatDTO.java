package ua.desktop.chat.messenger.domain.dto;

import ua.desktop.chat.messenger.domain.env.TypeChat;

import java.io.Serializable;
import java.util.Objects;

public class ChatDTO implements Serializable {
    private static final long serialVersionUID = 8683709059678144396L;
    private Long id;
    private String nameChat;
    private TypeChat typeChat;
    private UserDTO user;
    private Long userCompanionId;

    public ChatDTO(Long id, String nameChat, TypeChat typeChat, UserDTO user, Long userCompanionId) {
        this.id = id;
        this.nameChat = nameChat;
        this.typeChat = typeChat;
        this.user = user;
        this.userCompanionId = userCompanionId;
    }

    public ChatDTO(String nameChat, TypeChat typeChat, UserDTO user) {
        this.nameChat = nameChat;
        this.typeChat = typeChat;
        this.user = user;
    }

    public ChatDTO(TypeChat typeChat, Long userCompanionId, UserDTO user) {
        this.typeChat = typeChat;
        this.userCompanionId = userCompanionId;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameChat() {
        return nameChat;
    }

    public void setNameChat(String nameChat) {
        this.nameChat = nameChat;
    }

    public TypeChat getTypeChat() {
        return typeChat;
    }

    public void setTypeChat(TypeChat typeChat) {
        this.typeChat = typeChat;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Long getUserCompanionId() {
        return userCompanionId;
    }

    public void setUserCompanionId(Long userCompanionId) {
        this.userCompanionId = userCompanionId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ChatDTO chatDTO = (ChatDTO) object;
        return Objects.equals(id, chatDTO.id) && Objects.equals(nameChat, chatDTO.nameChat) && typeChat == chatDTO.typeChat && Objects.equals(user, chatDTO.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameChat, typeChat, user);
    }

    @Override
    public String toString() {
        return "ChatDTO{" +
                "id=" + id +
                ", nameChat='" + nameChat + '\'' +
                ", typeChat=" + typeChat +
                ", user=" + user +
                '}';
    }
}
