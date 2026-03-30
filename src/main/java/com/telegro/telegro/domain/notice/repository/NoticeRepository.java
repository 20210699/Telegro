package com.telegro.telegro.domain.notice.repository;

import com.telegro.telegro.domain.notice.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT n FROM Notice n " +
            "WHERE (:cursorCreatedAt IS NULL OR n.createdAt < :cursorCreatedAt OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId)) " +
            "ORDER BY n.createdAt DESC, n.id DESC")
    List<Notice> findNoticesWithCursor(
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
