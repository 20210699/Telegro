package com.telegro.telegro.domain.product.repository;

import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.product.entity.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageable);
}
