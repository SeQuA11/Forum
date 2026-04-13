package ru.Forum.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.Forum.api.dto.AuthRequestDTO;
import ru.Forum.api.dto.AuthResponseDTO;
import ru.Forum.api.dto.UserDTO;
import ru.Forum.api.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для регистрации и входа пользователей")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "409", description = "Пользователь уже существует")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody AuthRequestDTO dto) {
        UserDTO user = userService.register(dto);
        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    @ApiResponse(responseCode = "200", description = "Успешный вход")
    @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO dto) {
        String token = userService.authenticate(dto);

        UserDTO user = userService.findById(
                userService.loadUserByUsername(dto.getUsername()).hashCode()
        ).orElse(null);

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .username(dto.getUsername())
                .role(user != null ? user.getRole() : "USER")
                .userId(user != null ? user.getId() : null)
                .build();

        return ResponseEntity.ok(response);
    }
}