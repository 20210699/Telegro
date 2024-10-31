package com.telegro.telegro.domain.cart.repository;

import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserAndProduct(User user, Product product);

    Page<Cart> findAllByUser(User user, Pageable pageable);

    void deleteByIdAndUserId(Long cartId, Long userId);

    Optional<Cart> findByIdAndUserId(Long cartId, Long id);

    List<Cart> findByIdIn(List<Long> cartIds);
}
