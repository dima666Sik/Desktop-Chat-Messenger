package ua.desktop.chat.messenger.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message implements Serializable {
    private static final long serialVersionUID = -6924151770112288895L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    @Column(name = "local_date_time")
    private LocalDateTime localDateTime;
    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "id",nullable = false)
    private Chat chat;

    public Message() {
    }

    public Message(String message, LocalDateTime localDateTime, Chat chat) {
        this.message = message;
        this.localDateTime = localDateTime;
        this.chat = chat;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", localDateTime=" + localDateTime +
                ", chat=" + chat +
                '}';
    }
}
