package org.example.domain;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
public class ChatGroup implements Serializable {
    private static final long serialVersionUID = 1L;  // Add serialVersionUID

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int chatId;
    private String chatName;
    private String description;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    // Getters and Setters
    public int getChatId() { return chatId; }
    public void setChatId(int chatId) { this.chatId = chatId; }
    public String getChatName() { return chatName; }
    public void setChatName(String chatName) { this.chatName = chatName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }
}