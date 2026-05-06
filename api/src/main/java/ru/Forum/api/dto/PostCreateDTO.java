package ru.Forum.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostCreateDTO {

    @NotBlank(message = "Содержание обязательно")
    @Size(min = 1, max = 10000, message = "Содержание должно быть от 1 до 10000 символов")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}