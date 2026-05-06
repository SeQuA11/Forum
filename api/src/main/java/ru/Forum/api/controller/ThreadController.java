package ru.Forum.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.Forum.api.dto.ThreadCreateDTO;
import ru.Forum.api.dto.ThreadResponseDTO;
import ru.Forum.api.entity.ThreadStatus;
import ru.Forum.api.service.ThreadService;

@RestController
@RequestMapping("/api/v1/threads")
@RequiredArgsConstructor
@Tag(name = "Темы форума", description = "API для управления темами обсуждений")
public class ThreadController {

    private final ThreadService threadService;


    @GetMapping("/{id}")
    @Operation(summary = "Получить тему по ID")
    @ApiResponse(responseCode = "200", description = "Данные темы")
    @ApiResponse(responseCode = "404", description = "Тема не найдена")
    public ResponseEntity<ThreadResponseDTO> getThread(@PathVariable Long id) {
        return ResponseEntity.ok(threadService.getThreadById(id));
    }

    @PostMapping
    @Operation(summary = "Создать новую тему")
    @ApiResponse(responseCode = "201", description = "Тема создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    public ResponseEntity<ThreadResponseDTO> createThread(@Valid @RequestBody ThreadCreateDTO dto) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Требуется аутентификация для создания темы");
        }
        String username = authentication.getName();
        ThreadResponseDTO thread = threadService.create(dto, username);
        return ResponseEntity.status(201).body(thread);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить тему (автор или модератор)")
    @ApiResponse(responseCode = "200", description = "Тема обновлена")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    @ApiResponse(responseCode = "404", description = "Тема не найдена")
    public ResponseEntity<ThreadResponseDTO> updateThread(
            @PathVariable Long id,
            @Valid @RequestBody ThreadCreateDTO dto) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Требуется аутентификация для обновления темы");
        }
        String username = authentication.getName();
        ThreadResponseDTO updated = threadService.update(id, dto, username);
        return ResponseEntity.ok(updated);
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Operation(summary = "Изменить статус темы (модерация)")
    @ApiResponse(responseCode = "200", description = "Статус изменен")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    @ApiResponse(responseCode = "404", description = "Тема не найдена")
    public ResponseEntity<ThreadResponseDTO> updateThreadStatus(
            @PathVariable Long id,
            @RequestParam ThreadStatus status) {
        ThreadResponseDTO updated = threadService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/view")
    @Operation(summary = "Просмотр темы (увеличивает счетчик просмотров)")
    @ApiResponse(responseCode = "200", description = "Данные темы")
    @ApiResponse(responseCode = "404", description = "Тема не найдена")
    public ResponseEntity<ThreadResponseDTO> viewThread(@PathVariable Long id) {
        return ResponseEntity.ok(threadService.incrementViews(id));
    }

    @GetMapping
    @Operation(summary = "Получить список тем (с учётом прав)")
    @ApiResponse(responseCode = "200", description = "Список тем")
    public ResponseEntity<Page<ThreadResponseDTO>> getAllThreads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String username = null;
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                username = auth.getName();
            }
        } catch (Exception e) {
            // Анонимный пользователь
        }
        return ResponseEntity.ok(threadService.getActiveThreads(page, size, username));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Получить темы по категории (с учётом прав)")
    @ApiResponse(responseCode = "200", description = "Список тем категории")
    public ResponseEntity<Page<ThreadResponseDTO>> getThreadsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String username = null;
        try {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            // Анонимный пользователь
        }
        return ResponseEntity.ok(threadService.getThreadsByCategory(categoryId, page, size, username));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тему")
    @ApiResponse(responseCode = "204", description = "Тема удалена")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    @ApiResponse(responseCode = "404", description = "Тема не найдена")
    public ResponseEntity<Void> deleteThread(@PathVariable Long id) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Требуется аутентификация для удаления темы");
        }
        String username = authentication.getName();
        threadService.delete(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pin")
    @Operation(summary = "Закрепить/открепить тему для текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Статус закрепления изменен")
    @ApiResponse(responseCode = "404", description = "Тема не найдена")
    public ResponseEntity<ThreadResponseDTO> togglePin(@PathVariable Long id) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Требуется аутентификация для закрепления темы");
        }
        String username = authentication.getName();
        ThreadResponseDTO result = threadService.togglePin(id, username);
        return ResponseEntity.ok(result);
    }
}