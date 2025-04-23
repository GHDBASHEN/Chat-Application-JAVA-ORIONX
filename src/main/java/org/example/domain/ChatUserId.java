package org.example.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ChatUserId implements Serializable {
    private int userId;
    private int groupId;

    public ChatUserId() {}

    public ChatUserId(int userId, int groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    // hashCode and equals - required for composite key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatUserId)) return false;
        ChatUserId that = (ChatUserId) o;
        return userId == that.userId && groupId == that.groupId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }
}
