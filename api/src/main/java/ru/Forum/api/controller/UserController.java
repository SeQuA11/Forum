package ru.Forum.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.Forum.api.dto.UserDTO;
import ru.Forum.api.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список всех пользователей (только админ)")
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователя по ID (только админ)")
    @ApiResponse(responseCode = "200", description = "Данные пользователя")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + id)));
    }

    @PatchMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Заблокировать/разблокировать пользователя (только админ)")
    @ApiResponse(responseCode = "200", description = "Статус изменен")
    @ApiResponse(responseCode = "403", description = "Нет прав")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<UserDTO> banUser(@PathVariable Long id) {
        UserDTO updated = userService.banUser(id);
        return ResponseEntity.ok(updated);
    }
}