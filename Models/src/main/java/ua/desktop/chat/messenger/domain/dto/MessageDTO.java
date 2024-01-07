package ua.desktop.chat.messenger.domain.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class MessageDTO {
    private Long id;
    private String message;
    private LocalDateTime localDateTime;
    private ChatDTO chat;

    public MessageDTO(String message, LocalDateTime localDateTime, ChatDTO chat) {
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

    public ChatDTO getChat() {
        return chat;
    }

    public void setChat(ChatDTO chat) {
        this.chat = chat;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MessageDTO that = (MessageDTO) object;
        return Objects.equals(id, that.id) && Objects.equals(message, that.message) && Objects.equals(localDateTime, that.localDateTime) && Objects.equals(chat, that.chat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, localDateTime, chat);
    }

    @Override
    public String toString() {
        return "MessageDTO{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", localDateTime=" + localDateTime +
                ", chat=" + chat +
                '}';
    }
}
