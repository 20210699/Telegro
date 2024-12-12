package com.telegro.telegro.domain.order.repository;

import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 특정 사용자의 모든 주문 조회
    Page<Order> findByUser(User user, Pageable pageable);

    // 특정 기간 동안의 모든 주문 조회 (관리자)
    Page<Order> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);

    // 특정 사용자의 특정 기간 동안의 주문 조회
    Page<Order> findByCreatedAtBetweenAndUser(LocalDateTime startDateTime, LocalDateTime endDateTime, User user, Pageable pageable);

    // 특정 날짜 이후의 모든 주문 조회 (관리자)
    Page<Order> findByCreatedAtAfter(LocalDateTime startDateTime, Pageable pageable);

    // 특정 사용자의 특정 날짜 이후의 주문 조회
    Page<Order> findByCreatedAtAfterAndUser(LocalDateTime startDateTime, User user, Pageable pageable);

    // 특정 날짜 이전의 모든 주문 조회 (관리자)
    Page<Order> findByCreatedAtBefore(LocalDateTime endDateTime, Pageable pageable);

    // 특정 사용자의 특정 날짜 이전의 주문 조회
    Page<Order> findByCreatedAtBeforeAndUser(LocalDateTime endDateTime, User user, Pageable pageable);
}
