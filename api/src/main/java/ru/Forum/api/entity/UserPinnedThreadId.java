package ru.Forum.api.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserPinnedThreadId implements Serializable {

    private Long userId;
    private Long threadId;

    // Конструктор по умолчанию для JPA
    public UserPinnedThreadId() {}

    public UserPinnedThreadId(Long userId, Long threadId) {
        this.userId = userId;
        this.threadId = threadId;
    }

    // Геттеры и сеттеры
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPinnedThreadId that = (UserPinnedThreadId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(threadId, that.threadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, threadId);
    }
}
