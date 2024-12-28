package com.telegro.telegro.domain.user.entity;

import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity @Getter @Setter @Builder
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String username;

    @Column(length = 50, unique = true)
    private String userId;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String email;

    @Column
    private String address;

    @Column
    private String addressDetail;

    @Column
    private String zipCode;

    @Column
    private BigDecimal totalPrice;

    @Column
    private BigDecimal point;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryAddress> deliveryAddresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Company company;
}
