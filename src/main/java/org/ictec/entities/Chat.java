package org.ictec.entities;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int chatId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String filePath;

    // Getters, setters
}