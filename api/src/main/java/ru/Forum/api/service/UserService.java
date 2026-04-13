package ru.Forum.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Forum.api.dto.AuthRequestDTO;
import ru.Forum.api.dto.UserDTO;
import ru.Forum.api.entity.Role;
import ru.Forum.api.entity.User;
import ru.Forum.api.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO register(AuthRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Имя пользователя уже занято");
        }

        if (userRepository.existsByEmail(dto.getUsername())) {
            throw new RuntimeException("Email уже зарегистрирован");
        }

        // 👇 Используем конструктор + сеттеры вместо builder
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getUsername()); // Замените на dto.getEmail() если есть
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setIsBanned(false);

        userRepository.save(user);
        return convertToDTO(user);
    }

    public String authenticate(AuthRequestDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Неверный пароль");
        }

        if (user.getIsBanned()) {
            throw new RuntimeException("Пользователь заблокирован");
        }

        return "TOKEN_PLACEHOLDER"; // Замените на вызов jwtTokenProvider.generateToken()
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> findById(Long id) {
        return userRepository.findById(id).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setIsBanned(user.getIsBanned());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}