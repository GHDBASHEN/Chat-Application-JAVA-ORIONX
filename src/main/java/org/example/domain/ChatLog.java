package org.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Entity
public class ChatLog  implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int chat_id;
    private int user_id;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
    private String chatFilePath; // Path to the saved chat file

    public ChatLog() {
    }

    public int getChat_id() {
        return chat_id;
    }

    public void setChat_id(int chat_id) {
        this.chat_id = chat_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public LocalDateTime getStart_time() {
        return start_time;
    }

    public void setStart_time(LocalDateTime start_time) {
        this.start_time = start_time;
    }

    public LocalDateTime getEnd_time() {
        return end_time;
    }

    public void setEnd_time(LocalDateTime end_time) {
        this.end_time = end_time;
    }

    public String getChatFilePath() {
        return chatFilePath;
    }

    public void setChatFilePath(String chatFilePath) {
        this.chatFilePath = chatFilePath;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "chat_id=" + chat_id +
                ", user_id=" + user_id +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", chatFilePath='" + chatFilePath + '\'' +
                '}';
    }
}
