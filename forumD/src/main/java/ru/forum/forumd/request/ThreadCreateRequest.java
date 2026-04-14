package ru.forum.forumd.request;

public class ThreadCreateRequest {
    private String title;
    private String content;
    private Long categoryId;

    public ThreadCreateRequest() {}

    public ThreadCreateRequest(String title, String content, Long categoryId) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}