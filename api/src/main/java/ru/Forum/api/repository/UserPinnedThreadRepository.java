package ru.Forum.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.Forum.api.entity.UserPinnedThread;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPinnedThreadRepository extends JpaRepository<UserPinnedThread, Long> {
    
    Optional<UserPinnedThread> findByUserIdAndThreadId(Long userId, Long threadId);
    
    List<UserPinnedThread> findByUserId(Long userId);
    
    boolean existsByUserIdAndThreadId(Long userId, Long threadId);
    
    @Modifying
    @Query("DELETE FROM UserPinnedThread upt WHERE upt.user.id = :userId AND upt.thread.id = :threadId")
    void deleteByUserIdAndThreadId(@Param("userId") Long userId, @Param("threadId") Long threadId);
}
