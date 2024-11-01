package com.telegro.telegro.domain.cart.entity;

import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "carts")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Cart extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private Integer quantity;

    @Column
    private String selectOption;

    @Column
    private String inputOption;

    @Column
    private BigDecimal price; // 물건 개당 가격

    @Column
    private BigDecimal totalPrice; // 물건 전체 가격(개당 가격 * 수량)

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
