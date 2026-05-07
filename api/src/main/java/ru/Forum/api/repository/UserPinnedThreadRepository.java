package ru.Forum.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.Forum.api.entity.UserPinnedThread;
import ru.Forum.api.entity.UserPinnedThreadId;

import java.util.List;

@Repository
public interface UserPinnedThreadRepository extends JpaRepository<UserPinnedThread, UserPinnedThreadId> {

    List<UserPinnedThread> findById_UserId(Long userId);

    boolean existsById_UserIdAndId_ThreadId(Long userId, Long threadId);

    @Modifying
    @Query("DELETE FROM UserPinnedThread upt WHERE upt.id.userId = :userId AND upt.id.threadId = :threadId")
    void deleteByUserIdAndThreadId(Long userId, Long threadId);

    List<UserPinnedThread> findById_ThreadId(Long threadId);
}
