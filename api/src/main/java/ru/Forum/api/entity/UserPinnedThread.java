package ru.Forum.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_pinned_threads")
public class UserPinnedThread {

    @EmbeddedId
    private UserPinnedThreadId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Конструкторы
    public UserPinnedThread() {}

    public UserPinnedThread(UserPinnedThreadId id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    // Геттеры и сеттеры
    public UserPinnedThreadId getId() {
        return id;
    }

    public void setId(UserPinnedThreadId id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
