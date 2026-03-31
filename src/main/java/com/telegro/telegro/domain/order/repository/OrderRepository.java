package com.telegro.telegro.domain.order.repository;

import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.carts c " +
            "JOIN c.product p " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user) " +
            "AND (:query IS NULL OR p.productName LIKE %:query% OR p.productModel LIKE %:query%) " +
            "AND (:orderStatus IS NULL OR o.orderStatus = :orderStatus) " +
            "AND (:cursorCreatedAt IS NULL OR o.createdAt < :cursorCreatedAt OR (o.createdAt = :cursorCreatedAt AND o.id < :cursorId)) " +
            "ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findOrdersByProductAndQuery(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            @Param("query") String query,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o " +
            "JOIN o.carts c " +
            "JOIN c.product p " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user) " +
            "AND (o.paymentStatus = 'COMPLETED' OR o.paymentStatus = 'PENDING') " +
            "AND (:query IS NULL OR p.productName LIKE %:query% OR p.productModel LIKE %:query%) " +
            "AND (:orderStatus IS NULL OR o.orderStatus = :orderStatus)")
    BigDecimal findTotalAmountByProductAndQuery(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            @Param("query") String query,
            @Param("orderStatus") OrderStatus orderStatus
    );

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.user u " +
            "LEFT JOIN u.company c " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user) " +
            "AND (:query IS NULL OR u.username LIKE %:query% OR c.companyName LIKE %:query%) " +
            "AND (:orderStatus IS NULL OR o.orderStatus = :orderStatus) " +
            "AND (:cursorCreatedAt IS NULL OR o.createdAt < :cursorCreatedAt OR (o.createdAt = :cursorCreatedAt AND o.id < :cursorId)) " +
            "ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findOrdersByUserAndQuery(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            @Param("query") String query,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o " +
            "JOIN o.user u " +
            "LEFT JOIN u.company c " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user) " +
            "AND (o.paymentStatus = 'COMPLETED' OR o.paymentStatus = 'PENDING') " +
            "AND (:query IS NULL OR u.username LIKE %:query% OR c.companyName LIKE %:query%) " +
            "AND (:orderStatus IS NULL OR o.orderStatus = :orderStatus)")
    BigDecimal findTotalAmountByUserAndQuery(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            @Param("query") String query,
            @Param("orderStatus") OrderStatus orderStatus
    );

    @Query("SELECT o FROM Order o WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user) " +
            "AND (:orderStatus IS NULL OR o.orderStatus = :orderStatus) " +
            "AND (:cursorCreatedAt IS NULL OR o.createdAt < :cursorCreatedAt OR (o.createdAt = :cursorCreatedAt AND o.id < :cursorId)) " +
            "ORDER BY o.createdAt DESC, o.id DESC")
    List<Order> findOrdersByDateRangeAndUser(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (o.paymentStatus = 'COMPLETED' OR o.paymentStatus = 'PENDING') " +
            "AND (:user IS NULL OR o.user = :user) " +
            "AND (:orderStatus IS NULL OR o.orderStatus = :orderStatus)")
    BigDecimal findTotalAmountByDateRangeAndUser(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 @Param("user") User user,
                                                 @Param("orderStatus") OrderStatus orderStatus);

    Optional<Order> findByOrderNumber(String impUid);

    List<Order> findByPaymentStatusAndUpdatedAtBefore(PaymentStatus paymentStatus, LocalDateTime threshold);
}
