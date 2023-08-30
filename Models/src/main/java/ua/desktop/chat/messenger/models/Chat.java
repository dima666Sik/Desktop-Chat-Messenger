package ua.desktop.chat.messenger.models;

import jakarta.persistence.*;
import ua.desktop.chat.messenger.env.TypeChat;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "chats")
public class Chat implements Serializable {
    private static final long serialVersionUID = -5350423660998222481L;
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
