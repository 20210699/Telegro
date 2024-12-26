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
            "AND (p.productName LIKE %:query% OR p.productModel LIKE %:query%)")
    Page<Order> findOrdersByProductAndQuery(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o " +
            "JOIN o.carts c " +
            "JOIN c.product p " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user) " +
            "AND (o.paymentStatus = 'COMPLETED' OR o.paymentStatus = 'PENDING') " +
            "AND (p.productName LIKE %:query% OR p.productModel LIKE %:query%)")
    BigDecimal findTotalAmountByProductAndQuery(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            @Param("query") String query
    );

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.user u " +
            "JOIN u.company c " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user) " +
            "AND (u.username LIKE %:query% OR c.companyName LIKE %:query%)")
    Page<Order> findOrdersByUserAndQuery(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o " +
            "JOIN o.user u " +
            "JOIN u.company c " +
            "WHERE (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:user IS NULL OR o.user = :user) " +
            "AND (o.paymentStatus = 'COMPLETED' OR o.paymentStatus = 'PENDING') " +
            "AND (u.username LIKE %:query% OR c.companyName LIKE %:query%)")
    BigDecimal findTotalAmountByUserAndQuery(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("user") User user,
            @Param("query") String query
    );

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
            "AND (o.paymentStatus = 'COMPLETED' OR o.paymentStatus = 'PENDING') " +
            "AND (:user IS NULL OR o.user = :user)")
    BigDecimal findTotalAmountByDateRangeAndUser(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 @Param("user") User user);

    Optional<Order> findByOrderNumber(String impUid);

    List<Order> findByPaymentStatusAndUpdatedAtBefore(PaymentStatus paymentStatus, LocalDateTime threshold);
}
