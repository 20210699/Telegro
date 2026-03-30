package com.telegro.telegro.domain.cart.repository;

import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.entity.enums.CartStatus;
import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserAndProduct(User user, Product product);

    @Query("SELECT c FROM Cart c " +
            "WHERE c.user = :user " +
            "AND c.cartStatus = 'IN_CART' " +
            "AND (:cursorCreatedAt IS NULL OR c.createdAt < :cursorCreatedAt OR (c.createdAt = :cursorCreatedAt AND c.id < :cursorId)) " +
            "ORDER BY c.createdAt DESC, c.id DESC")
    List<Cart> findAllInCartByUserWithCursor(
            @Param("user") User user,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("SELECT c FROM Cart c WHERE c.user = :user AND c.cartStatus = 'ORDERED'")
    List<Cart> findAllOrderedByUser(User user);

    void deleteByIdAndUserId(Long cartId, Long userId);

    Optional<Cart> findByIdAndUserId(Long cartId, Long id);

    List<Cart> findByIdIn(List<Long> cartIds);

    void deleteByUser(User user);

    void deleteByProductId(Long productId);

    long countByUserAndCartStatus(User user, CartStatus cartStatus);
}
