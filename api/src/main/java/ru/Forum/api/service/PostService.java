package ru.Forum.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Forum.api.dto.PostCreateDTO;
import ru.Forum.api.dto.PostResponseDTO;
import ru.Forum.api.entity.Post;
import ru.Forum.api.entity.Thread;
import ru.Forum.api.entity.User;
import ru.Forum.api.repository.PostRepository;
import ru.Forum.api.repository.ThreadRepository;
import ru.Forum.api.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, ThreadRepository threadRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.threadRepository = threadRepository;
        this.userRepository = userRepository;
    }


    @Transactional(readOnly = true)
    public Page<PostResponseDTO> getPostsByThread(Long threadId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return postRepository.findByThreadId(threadId, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public PostResponseDTO create(Long threadId, PostCreateDTO dto, String username) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Тема не найдена: " + threadId));

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));

        Post post = new Post();
        post.setContent(dto.getContent());
        post.setThread(thread);
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(postRepository.save(post));
    }

    private PostResponseDTO convertToDTO(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setThreadId(post.getThread().getId());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorUsername(post.getAuthor().getUsername());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        return dto;
    }
}