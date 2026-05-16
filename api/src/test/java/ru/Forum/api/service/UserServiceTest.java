package ru.Forum.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.Forum.api.dto.AuthRequestDTO;
import ru.Forum.api.dto.UserDTO;
import ru.Forum.api.entity.Role;
import ru.Forum.api.entity.User;
import ru.Forum.api.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AuthRequestDTO authRequestDTO;
    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername("testuser");
        authRequestDTO.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPasswordHash("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setIsBanned(false);
        testUser.setCreatedAt(LocalDateTime.now());

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("testuser@example.com");
        testUserDTO.setRole("USER");
        testUserDTO.setIsBanned(false);
        testUserDTO.setCreatedAt(testUser.getCreatedAt());
    }

    // ==================== ПОЗИТИВНЫЕ ТЕСТЫ ====================

    @Test
    @DisplayName("Позитивный тест 1: Успешная регистрация нового пользователя")
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByUsername(authRequestDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(authRequestDTO.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(authRequestDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        UserDTO result = userService.register(authRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("testuser", result.getEmail()); // Email устанавливается из username в методе register
        assertEquals("USER", result.getRole());
        assertFalse(result.getIsBanned());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(authRequestDTO.getPassword());
    }

    @Test
    @DisplayName("Позитивный тест 2: Успешная аутентификация пользователя")
    void testAuthenticate_Success() {
        // Arrange
        when(userRepository.findByUsername(authRequestDTO.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequestDTO.getPassword(), testUser.getPasswordHash())).thenReturn(true);

        // Act
        String token = userService.authenticate(authRequestDTO);

        // Assert
        assertNotNull(token);
        assertEquals("TOKEN_PLACEHOLDER", token);
        verify(userRepository, times(1)).findByUsername(authRequestDTO.getUsername());
    }

    @Test
    @DisplayName("Позитивный тест 3: Успешный поиск пользователя по ID")
    void testFindById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserDTO> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Позитивный тест 4: Успешный поиск пользователя по имени")
    void testFindByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.findByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Позитивный тест 5: Успешное получение всех пользователей")
    void testGetAllUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("pass2");
        user2.setRole(Role.USER);
        user2.setIsBanned(false);
        user2.setCreatedAt(LocalDateTime.now());

        when(userRepository.findAll()).thenReturn(List.of(testUser, user2));

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    // ==================== НЕГАТИВНЫЕ ТЕСТЫ ====================

    @Test
    @DisplayName("Негативный тест 1: Регистрация с занятым именем пользователя")
    void testRegister_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(authRequestDTO.getUsername())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(authRequestDTO);
        });
        assertEquals("Имя пользователя уже занято", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Негативный тест 2: Регистрация с пустым именем пользователя")
    void testRegister_EmptyUsername() {
        // Arrange
        AuthRequestDTO emptyUsernameRequest = new AuthRequestDTO();
        emptyUsernameRequest.setUsername("");
        emptyUsernameRequest.setPassword("password123");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(emptyUsernameRequest);
        });
        assertEquals("Имя пользователя не может быть пустым", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Негативный тест 3: Аутентификация с неверным паролем")
    void testAuthenticate_WrongPassword() {
        // Arrange
        when(userRepository.findByUsername(authRequestDTO.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequestDTO.getPassword(), testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.authenticate(authRequestDTO);
        });
        assertEquals("Неверный пароль", exception.getMessage());
    }

    @Test
    @DisplayName("Негативный тест 4: Аутентификация заблокированного пользователя")
    void testAuthenticate_BannedUser() {
        // Arrange
        testUser.setIsBanned(true);
        when(userRepository.findByUsername(authRequestDTO.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequestDTO.getPassword(), testUser.getPasswordHash())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.authenticate(authRequestDTO);
        });
        assertEquals("Пользователь заблокирован", exception.getMessage());
    }

    @Test
    @DisplayName("Негативный тест 5: Поиск несуществующего пользователя по ID")
    void testFindById_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999L);
    }
}
