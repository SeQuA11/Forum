package ru.Forum.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.Forum.api.dto.CategoryDTO;
import ru.Forum.api.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Категории", description = "API для управления разделами форума")
public class CategoryController {

    private final CategoryService categoryService;

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
    @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        CategoryDTO created = categoryService.create(dto);
        return ResponseEntity.status(201).body(created);
    }
}