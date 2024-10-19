package com.telegro.telegro.domain.cart.repository;

import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserAndProduct(User user, Product product);
}
