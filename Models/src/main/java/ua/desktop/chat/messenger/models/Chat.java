package ua.desktop.chat.messenger.models;

import jakarta.persistence.*;
import ua.desktop.chat.messenger.env.TypeChat;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name_chat")
    private String nameChat;
    @Enumerated(EnumType.STRING)
    @Column(name = "type_chat")
    private TypeChat typeChat;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY)
    private List<Message> messageList;

    public Chat() {
    }

    public Chat(String nameChat, TypeChat typeChat, User user) {
        this.nameChat = nameChat;
        this.typeChat = typeChat;
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


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Chat chat = (Chat) object;
        return Objects.equals(id, chat.id) && Objects.equals(nameChat, chat.nameChat) && typeChat == chat.typeChat && Objects.equals(user, chat.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameChat, typeChat, user);
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", nameChat='" + nameChat + '\'' +
                ", typeChat=" + typeChat +
                ", user=" + user +
                ", messageList=" + messageList +
                '}';
    }
}
