package ru.Forum.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_pinned_threads", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "thread_id"}))
public class UserPinnedThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;

    @Column(name = "pinned_at", nullable = false, updatable = false)
    private LocalDateTime pinnedAt = LocalDateTime.now();

    // Конструкторы
    public UserPinnedThread() {}

    public UserPinnedThread(User user, Thread thread) {
        this.user = user;
        this.thread = thread;
        this.pinnedAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public LocalDateTime getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(LocalDateTime pinnedAt) {
        this.pinnedAt = pinnedAt;
    }
}
