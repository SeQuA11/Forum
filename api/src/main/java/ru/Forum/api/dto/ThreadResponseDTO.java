package ru.Forum.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ThreadResponseDTO {
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorUsername;
    private String status;
    private Integer viewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPinnedByCurrentUser;
    private List<Long> pinnedByUserIds;

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsPinnedByCurrentUser() {
        return isPinnedByCurrentUser;
    }

    public void setIsPinnedByCurrentUser(Boolean isPinnedByCurrentUser) {
        this.isPinnedByCurrentUser = isPinnedByCurrentUser;
    }

    public List<Long> getPinnedByUserIds() {
        return pinnedByUserIds;
    }

    public void setPinnedByUserIds(List<Long> pinnedByUserIds) {
        this.pinnedByUserIds = pinnedByUserIds;
    }
}