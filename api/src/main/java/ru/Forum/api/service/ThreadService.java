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
import ru.Forum.api.entity.Category;
import ru.Forum.api.entity.Thread;
import ru.Forum.api.entity.ThreadStatus;
import ru.Forum.api.entity.User;
import ru.Forum.api.repository.CategoryRepository;
import ru.Forum.api.repository.ThreadRepository;
import ru.Forum.api.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ThreadService(ThreadRepository threadRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.threadRepository = threadRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

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

    @Transactional
    public ThreadResponseDTO create(ThreadCreateDTO dto, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        Thread thread = new Thread();
        thread.setTitle(dto.getTitle());
        thread.setContent(dto.getContent());
        thread.setCategory(category);
        thread.setAuthor(author);
        thread.setStatus(ThreadStatus.ACTIVE);
        thread.setViewsCount(0);

        return convertToDTO(threadRepository.save(thread));
    }

    @Transactional(readOnly = true)
    public ThreadResponseDTO incrementViews(Long id) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тема не найдена"));

        thread.setViewsCount(thread.getViewsCount() + 1);
        return convertToDTO(thread);
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
        return dto;
    }
}