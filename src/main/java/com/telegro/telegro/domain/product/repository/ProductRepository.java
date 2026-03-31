package com.telegro.telegro.domain.product.repository;

import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.product.entity.enums.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p " +
            "WHERE p.category = :category " +
            "AND (:cursorCreatedAt IS NULL OR p.createdAt < :cursorCreatedAt OR (p.createdAt = :cursorCreatedAt AND p.id < :cursorId)) " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<Product> findByCategoryWithCursor(
            @Param("category") Category category,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
