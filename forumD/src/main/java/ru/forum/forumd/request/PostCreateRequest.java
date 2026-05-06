package ru.forum.forumd.request;

public class PostCreateRequest {
    private String content;
    private Long threadId;

    public PostCreateRequest() {}

    public PostCreateRequest(String content, Long threadId) {
        this.content = content;
        this.threadId = threadId;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getThreadId() { return threadId; }
    public void setThreadId(Long threadId) { this.threadId = threadId; }
}