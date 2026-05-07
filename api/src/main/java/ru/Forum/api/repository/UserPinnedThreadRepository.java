package ru.Forum.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.Forum.api.entity.UserPinnedThread;
import ru.Forum.api.entity.UserPinnedThreadId;

import java.util.List;

@Repository
public interface UserPinnedThreadRepository extends JpaRepository<UserPinnedThread, UserPinnedThreadId> {

    @Query("SELECT upt.thread.id FROM UserPinnedThread upt WHERE upt.user.id = :userId")
    List<Long> findPinnedThreadIdsByUserId(Long userId);

    boolean existsByUserIdAndThreadId(Long userId, Long threadId);

    void deleteByUserIdAndThreadId(Long userId, Long threadId);
}
