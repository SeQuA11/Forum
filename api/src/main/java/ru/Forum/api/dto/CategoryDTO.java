package ru.Forum.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Название категории обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    @NotBlank(message = "Slug обязателен")
    private String slug;

    private Integer sortOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
}