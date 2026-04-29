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

    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }


    @GetMapping("/{id}")
    @Operation(summary = "Получить тему по ID")
    @ApiResponse(responseCode = "200", description = "Данные темы")
    @ApiResponse(responseCode = "404", description = "Тема не найдена")
    public ResponseEntity<ThreadResponseDTO> getThread(@PathVariable Long id) {
        return ResponseEntity.ok(threadService.getThreadById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @Operation(summary = "Создать новую тему")
    @ApiResponse(responseCode = "201", description = "Тема создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "403", description = "Нет прав доступа")
    public ResponseEntity<ThreadResponseDTO> createThread(@Valid @RequestBody ThreadCreateDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ThreadResponseDTO thread = threadService.create(dto, username);
        return ResponseEntity.status(201).body(thread);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @Operation(summary = "Обновить тему (автор или модератор)")
    @ApiResponse(responseCode = "200", description = "Тема обновлена")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    @ApiResponse(responseCode = "404", description = "Тема не найдена")
    public ResponseEntity<ThreadResponseDTO> updateThread(
            @PathVariable Long id,
            @Valid @RequestBody ThreadCreateDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
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
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @Operation(summary = "Получить список тем (с учётом прав)")
    @ApiResponse(responseCode = "200", description = "Список тем")
    public ResponseEntity<Page<ThreadResponseDTO>> getAllThreads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(threadService.getActiveThreads(page, size, username));
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @Operation(summary = "Получить темы по категории (с учётом прав)")
    @ApiResponse(responseCode = "200", description = "Список тем категории")
    public ResponseEntity<Page<ThreadResponseDTO>> getThreadsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(threadService.getThreadsByCategory(categoryId, page, size, username));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @Operation(summary = "Удалить тему")
    @ApiResponse(responseCode = "204", description = "Тема удалена")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    @ApiResponse(responseCode = "404", description = "Тема не найдена")
    public ResponseEntity<Void> deleteThread(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        threadService.delete(id, username);
        return ResponseEntity.noContent().build();
    }
}