package ru.Forum.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_pinned_threads")
public class UserPinnedThread {

    @EmbeddedId
    private UserPinnedThreadId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("threadId")
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Конструкторы
    public UserPinnedThread() {}

    public UserPinnedThread(User user, Thread thread) {
        this.user = user;
        this.thread = thread;
        this.id = new UserPinnedThreadId(user.getId(), thread.getId());
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public UserPinnedThreadId getId() {
        return id;
    }

    public void setId(UserPinnedThreadId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
