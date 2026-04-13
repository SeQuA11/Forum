package ru.Forum.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ThreadCreateDTO {

    @NotBlank(message = "Заголовок обязателен")
    @Size(min = 5, max = 255, message = "Заголовок должен быть от 5 до 255 символов")
    private String title;

    @NotBlank(message = "Содержание обязательно")
    @Size(min = 10, message = "Содержание должно содержать минимум 10 символов")
    private String content;

    @NotNull(message = "Категория обязательна")
    private Long categoryId;
}