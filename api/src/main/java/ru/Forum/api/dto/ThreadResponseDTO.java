package ru.Forum.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
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
}