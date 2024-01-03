package ua.desktop.chat.messenger.model;

import jakarta.persistence.*;
import ua.desktop.chat.messenger.dto.MessageDTO;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "messages")
public class Message {
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

    public Message(MessageDTO messageDTO, Chat chat) {
        this.message = messageDTO.getMessage();
        this.localDateTime = messageDTO.getLocalDateTime();
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
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Message message1 = (Message) object;
        return Objects.equals(id, message1.id) && Objects.equals(message, message1.message) && Objects.equals(localDateTime, message1.localDateTime) && Objects.equals(chat, message1.chat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, localDateTime, chat);
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
