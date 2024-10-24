package com.telegro.telegro.domain.user.repository;

import com.telegro.telegro.domain.user.entity.Hit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {
    // 특정 날짜와 로그인 사용자 ID로 방문 기록 조회
    Optional<Hit> findByDateAndUserId(LocalDate date, Long userId);

    // 특정 날짜와 익명 사용자 ID로 방문 기록 조회
    Optional<Hit> findByDateAndAnonymousUserId(LocalDate date, String anonymousUserId);

    // 날짜별 모든 방문 기록 조회 (관리자 분석용)
    Optional<List<Hit>> findByDate(LocalDate date);
}
