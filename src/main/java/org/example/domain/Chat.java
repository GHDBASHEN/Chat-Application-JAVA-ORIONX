package org.example.domain;

import java.time.LocalDateTime;

public class Chat {
    private int chatId;
    private String chatName;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime endTime;

    // Constructors
    public Chat() {
        this.createdTime = LocalDateTime.now();
    }

    public Chat(String chatName, String description) {
        this();
        this.chatName = chatName;
        this.description = description;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return chatName + " - " + description;
    }
}