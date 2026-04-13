package ru.Forum.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Forum.api.dto.AuthRequestDTO;
import ru.Forum.api.dto.UserDTO;
import ru.Forum.api.entity.Role;
import ru.Forum.api.entity.User;
import ru.Forum.api.repository.UserRepository;
import ru.Forum.api.security.JwtTokenProvider;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }

    @Transactional
    public UserDTO register(AuthRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Имя пользователя уже занято");
        }

        if (userRepository.existsByEmail(dto.getUsername())) {
            throw new RuntimeException("Email уже зарегистрирован");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .isBanned(false)
                .build();

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

        return jwtTokenProvider.generateToken(user.getUsername());
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> findById(Long id) {
        return userRepository.findById(id).map(this::convertToDTO);
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