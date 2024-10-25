package com.telegro.telegro.domain.user.repository;

import com.telegro.telegro.domain.user.dto.hitDTO;
import com.telegro.telegro.domain.user.entity.Hit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {

    Optional<Hit> findByDateAndUserId(LocalDate date, Long userId);

    Optional<Hit> findByDateAndAnonymousUserId(LocalDate date, String anonymousUserId);

    List<Hit> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
