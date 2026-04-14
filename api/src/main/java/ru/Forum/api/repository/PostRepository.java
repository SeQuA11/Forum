package ru.Forum.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.Forum.api.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByThreadId(Long threadId, Pageable pageable);
}