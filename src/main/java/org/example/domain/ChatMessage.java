package org.example.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage  implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int message_id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String message;
    private LocalDateTime send_at;

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSend_at() {
        return send_at;
    }

    public void setSend_at(LocalDateTime send_at) {
        this.send_at = send_at;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "message_id=" + message_id +
                ", user=" + user +
                ", message='" + message + '\'' +
                ", send_at=" + send_at +
                '}';
    }
}