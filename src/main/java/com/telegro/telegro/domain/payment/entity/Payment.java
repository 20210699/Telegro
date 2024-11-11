package com.telegro.telegro.domain.payment.entity;

import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id; // PK

    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User user; // 사용자

    @ManyToOne
    @JoinColumn(name = "orders", nullable = false)
    private Order order; // 주문 테이블과 다대일 (연관관계 주인은 주문)

//    @ManyToOne
//    @JoinColumn(name = "product", nullable = false)
//    private Product product; // 상품

    @Column(name = "product_name")
    private String productName; // 상품 이름

    @Column(name = "product_option")
    private String productOption; // 상품 옵션

    @Column(name = "product_price", nullable = false)
    private Integer price; // 가격

    @Column
    private Long totalPrice; // 결제한 총 가격

    @Column
    private LocalDateTime paidAt; // 결제시각

    @Column
    private Boolean status = true; // 상태
}
