package org.example.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.io.Serializable;

@Entity
@Table(name = "chat_user")
public class ChatUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ChatUserId id = new ChatUserId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", referencedColumnName = "user_id",
            foreignKey = @ForeignKey(name = "FK_chat_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id", referencedColumnName = "chatId",
            foreignKey = @ForeignKey(name = "FK_chat_group",
                    foreignKeyDefinition = "FOREIGN KEY (group_id) REFERENCES chatgroup(chatId) ON DELETE CASCADE"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatGroup chatGroup;

    // Getters and Setters remain the same
    public ChatUserId getId() { return id; }
    public void setId(ChatUserId id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ChatGroup getChatGroup() { return chatGroup; }
    public void setChatGroup(ChatGroup chatGroup) { this.chatGroup = chatGroup; }
}