package ru.Forum.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.Forum.api.entity.Thread;
import ru.Forum.api.entity.ThreadStatus;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    Page<Thread> findByCategoryIdAndStatus(Long categoryId, ThreadStatus status, Pageable pageable);
    List<Thread> findByStatus(ThreadStatus status);

    @Query("SELECT t FROM Thread t WHERE t.status = 'ACTIVE' ORDER BY t.createdAt DESC")
    Page<Thread> findActiveThreads(Pageable pageable);
}