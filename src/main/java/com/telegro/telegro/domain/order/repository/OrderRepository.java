package com.telegro.telegro.domain.order.repository;

import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user)")
    Page<Order> findOrdersByDateRangeAndUser(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user)")
    BigDecimal findTotalAmountByDateRangeAndUser(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 @Param("user") User user);

    Optional<Order> findByOrderNumber(String impUid);

    List<Order> findByPaymentStatusAndUpdatedAtBefore(PaymentStatus paymentStatus, LocalDateTime threshold);
}
