package ru.Forum.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.Forum.api.dto.PostCreateDTO;
import ru.Forum.api.dto.PostResponseDTO;
import ru.Forum.api.service.PostService;

@RestController
@RequestMapping("/api/v1/threads/{threadId}/posts")
@Tag(name = "Сообщения", description = "API для управления сообщениями в темах")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }


    @GetMapping
    @Operation(summary = "Получить все сообщения темы")
    @ApiResponse(responseCode = "200", description = "Список сообщений")
    public ResponseEntity<Page<PostResponseDTO>> getPosts(
            @PathVariable Long threadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(postService.getPostsByThread(threadId, page, size));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @Operation(summary = "Создать новое сообщение")
    @ApiResponse(responseCode = "201", description = "Сообщение создано")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    public ResponseEntity<PostResponseDTO> createPost(
            @PathVariable Long threadId,
            @Valid @RequestBody PostCreateDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PostResponseDTO post = postService.create(threadId, dto, username);
        return ResponseEntity.status(201).body(post);
    }
}