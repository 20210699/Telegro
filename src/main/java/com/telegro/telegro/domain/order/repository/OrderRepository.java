package com.telegro.telegro.domain.order.repository;

import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCreatedAtBetweenAndUser(LocalDateTime startDateTime, LocalDateTime EndDateTime, User user, Pageable pageable);

    Page<Order> findByCreatedAtAfterAndUser(LocalDateTime startDateTime, User user, Pageable pageable);

    Page<Order> findByCreatedAtBeforeAndUser(LocalDateTime endDateTime, User user, Pageable pageable);

    Page<Order> findByUser(User user, Pageable pageable);
}
