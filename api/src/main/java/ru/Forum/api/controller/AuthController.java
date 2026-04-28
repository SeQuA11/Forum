package ru.Forum.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import ru.Forum.api.dto.AuthRequestDTO;
import ru.Forum.api.dto.AuthResponseDTO;
import ru.Forum.api.dto.UserDTO;
import ru.Forum.api.security.JwtTokenProvider;
import ru.Forum.api.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для регистрации и входа пользователей")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

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
        // Аутентификация
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Генерация JWT токена
        String token = jwtTokenProvider.generateToken(dto.getUsername());

        // Получение данных пользователя
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());

        // Получение ID пользователя из UserService
        UserDTO userDTO = userService.findByUsername(dto.getUsername());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setUsername(dto.getUsername());
        response.setRole(userDetails.getAuthorities().iterator().next().getAuthority());
        response.setUserId(userDTO.getId());

        return ResponseEntity.ok(response);
    }
}