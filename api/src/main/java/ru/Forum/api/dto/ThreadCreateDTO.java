package ru.Forum.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ThreadCreateDTO {

    @NotBlank(message = "Заголовок обязателен")
    @Size(min = 5, max = 255, message = "Заголовок должен быть от 5 до 255 символов")
    private String title;

    @NotBlank(message = "Содержание обязательно")
    @Size(min = 10, message = "Содержание должно содержать минимум 10 символов")
    private String content;

    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    // Геттеры и сеттеры
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
}