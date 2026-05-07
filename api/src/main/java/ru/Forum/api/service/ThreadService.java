package ru.Forum.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Forum.api.dto.ThreadCreateDTO;
import ru.Forum.api.dto.ThreadResponseDTO;
import ru.Forum.api.entity.*;
import ru.Forum.api.entity.Thread;
import ru.Forum.api.repository.CategoryRepository;
import ru.Forum.api.repository.ThreadRepository;
import ru.Forum.api.repository.UserRepository;
import ru.Forum.api.repository.UserPinnedThreadRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserPinnedThreadRepository userPinnedThreadRepository;


    @Transactional(readOnly = true)
    public Page<ThreadResponseDTO> getActiveThreads(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return threadRepository.findActiveThreads(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ThreadResponseDTO> getThreadsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return threadRepository.findByCategoryIdAndStatus(categoryId, ThreadStatus.ACTIVE, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public ThreadResponseDTO getThreadById(Long id) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тема не найдена: " + id));
        return convertToDTO(thread);
    }

    @Transactional
    public ThreadResponseDTO create(ThreadCreateDTO dto, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категория не найдена: " + dto.getCategoryId()));

        Thread thread = new Thread();
        thread.setTitle(dto.getTitle());
        thread.setContent(dto.getContent());
        thread.setCategory(category);
        thread.setAuthor(author);
        thread.setStatus(ThreadStatus.ACTIVE);
        thread.setViewsCount(0);
        thread.setCreatedAt(LocalDateTime.now());
        thread.setUpdatedAt(LocalDateTime.now());  // ВАЖНО!

        return convertToDTO(threadRepository.save(thread));
    }

    @Transactional
    public ThreadResponseDTO update(Long id, ThreadCreateDTO dto, String username) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тема не найдена: " + id));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка прав: только автор, модератор или админ могут редактировать
        if (!thread.getAuthor().getUsername().equals(username) &&
                !currentUser.getRole().name().equals("MODERATOR") &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Нет прав на редактирование этой темы");
        }

        thread.setTitle(dto.getTitle());
        thread.setContent(dto.getContent());

        return convertToDTO(threadRepository.save(thread));
    }


    @Transactional
    public ThreadResponseDTO updateStatus(Long id, ThreadStatus status) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тема не найдена: " + id));

        thread.setStatus(status);
        return convertToDTO(threadRepository.save(thread));
    }

    @Transactional
    public ThreadResponseDTO incrementViews(Long id) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тема не найдена: " + id));

        thread.setViewsCount(thread.getViewsCount() + 1);
        return convertToDTO(threadRepository.save(thread));
    }

    @Transactional(readOnly = true)
    public Page<ThreadResponseDTO> getActiveThreads(int page, int size, String username) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Если пользователь не аутентифицирован, показываем только ACTIVE темы
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            Page<Thread> threads = threadRepository.findByStatus(ThreadStatus.ACTIVE, pageable);
            return threads.map(this::convertToDTO);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));

        // Админы видят всё, модераторы не видят HIDDEN, обычные пользователи видят только ACTIVE
        Page<Thread> threads;
        if (user.getRole() == Role.ADMIN) {
            threads = threadRepository.findAll(pageable);
        } else if (user.getRole() == Role.MODERATOR) {
            threads = threadRepository.findByStatusNot(ThreadStatus.HIDDEN, pageable);
        } else {
            threads = threadRepository.findByStatus(ThreadStatus.ACTIVE, pageable);
        }

        // Получаем список закрепленных тем для текущего пользователя
        List<Long> pinnedThreadIds = userPinnedThreadRepository.findPinnedThreadIdsByUserId(user.getId());

        return threads.map(thread -> {
            ThreadResponseDTO dto = convertToDTO(thread);
            dto.setPinnedByCurrentUser(pinnedThreadIds.contains(thread.getId()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<ThreadResponseDTO> getThreadsByCategory(Long categoryId, int page, int size, String username) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Если пользователь не аутентифицирован, показываем только ACTIVE темы
        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            Page<Thread> threads = threadRepository.findByCategoryIdAndStatus(categoryId, ThreadStatus.ACTIVE, pageable);
            return threads.map(this::convertToDTO);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));

        Page<Thread> threads;
        if (user.getRole() == Role.ADMIN) {
            threads = threadRepository.findByCategoryId(categoryId, pageable);
        } else if (user.getRole() == Role.MODERATOR) {
            threads = threadRepository.findByCategoryIdAndStatusNot(categoryId, ThreadStatus.HIDDEN, pageable);
        } else {
            threads = threadRepository.findByCategoryIdAndStatus(categoryId, ThreadStatus.ACTIVE, pageable);
        }

        // Получаем список закрепленных тем для текущего пользователя
        List<Long> pinnedThreadIds = userPinnedThreadRepository.findPinnedThreadIdsByUserId(user.getId());

        return threads.map(thread -> {
            ThreadResponseDTO dto = convertToDTO(thread);
            dto.setPinnedByCurrentUser(pinnedThreadIds.contains(thread.getId()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public ThreadResponseDTO getThreadByIdWithPinnedStatus(Long id, String username) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тема не найдена: " + id));
        
        ThreadResponseDTO dto = convertToDTO(thread);
        
        if (username != null && !username.isEmpty() && !"anonymousUser".equals(username)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
            boolean isPinned = userPinnedThreadRepository.existsByUserIdAndThreadId(user.getId(), id);
            dto.setPinnedByCurrentUser(isPinned);
        }
        
        return dto;
    }

    @Transactional
    public void togglePin(Long threadId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
        
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Тема не найдена: " + threadId));

        boolean isPinned = userPinnedThreadRepository.existsByUserIdAndThreadId(user.getId(), threadId);
        
        if (isPinned) {
            userPinnedThreadRepository.deleteByUserIdAndThreadId(user.getId(), threadId);
        } else {
            UserPinnedThread pinned = new UserPinnedThread(user, thread);
            userPinnedThreadRepository.save(pinned);
        }
    }

    // Метод для удаления темы
    @Transactional
    public void delete(Long id, String username) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тема не найдена: " + id));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Удалять может: автор, модератор или админ
        if (!thread.getAuthor().getUsername().equals(username) &&
                currentUser.getRole() != Role.MODERATOR &&
                currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Нет прав на удаление этой темы");
        }

        threadRepository.delete(thread);
    }

    private ThreadResponseDTO convertToDTO(Thread thread) {
        ThreadResponseDTO dto = new ThreadResponseDTO();
        dto.setId(thread.getId());
        dto.setTitle(thread.getTitle());
        dto.setContent(thread.getContent());
        dto.setCategoryId(thread.getCategory().getId());
        dto.setCategoryName(thread.getCategory().getName());
        dto.setAuthorId(thread.getAuthor().getId());
        dto.setAuthorUsername(thread.getAuthor().getUsername());
        dto.setStatus(thread.getStatus().name());
        dto.setViewsCount(thread.getViewsCount());
        dto.setCreatedAt(thread.getCreatedAt());
        dto.setUpdatedAt(thread.getUpdatedAt());
        dto.setPinnedByCurrentUser(false); // По умолчанию false, устанавливается в методах списка
        return dto;
    }
}