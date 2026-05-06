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
    List<Thread> findByStatus(ThreadStatus status);
    Page<Thread> findByStatus(ThreadStatus status, Pageable pageable);
    Page<Thread> findByStatusNot(ThreadStatus status, Pageable pageable);
    Page<Thread> findByCategoryIdAndStatus(Long categoryId, ThreadStatus status, Pageable pageable);
    Page<Thread> findByCategoryIdAndStatusNot(Long categoryId, ThreadStatus status, Pageable pageable);
    Page<Thread> findByCategoryId(Long categoryId, Pageable pageable);
    long countByCategoryId(Long categoryId);

    @Query("SELECT t FROM Thread t WHERE t.status = 'ACTIVE' ORDER BY t.createdAt DESC")
    Page<Thread> findActiveThreads(Pageable pageable);
}