package ru.Forum.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.Forum.api.dto.CategoryDTO;
import ru.Forum.api.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Категории", description = "API для управления разделами форума")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping
    @Operation(summary = "Получить список активных категорий")
    @ApiResponse(responseCode = "200", description = "Список категорий")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую категорию (только для админов)")
    @ApiResponse(responseCode = "201", description = "Категория создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        CategoryDTO created = categoryService.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить категорию (только для админов)")
    @ApiResponse(responseCode = "200", description = "Категория обновлена")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    @ApiResponse(responseCode = "404", description = "Категория не найдена")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO dto) {
        CategoryDTO updated = categoryService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить категорию (только для админов)")
    @ApiResponse(responseCode = "204", description = "Категория удалена")
    @ApiResponse(responseCode = "400", description = "Категория не пуста")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    @ApiResponse(responseCode = "404", description = "Категория не найдена")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}