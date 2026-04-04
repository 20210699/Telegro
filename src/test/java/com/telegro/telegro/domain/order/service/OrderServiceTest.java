package com.telegro.telegro.domain.order.service;

import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.repository.OrderRepository;
import com.telegro.telegro.domain.user.repository.DeliveryAddressRepository;
import com.telegro.telegro.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryAddressRepository deliveryAddressRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void expireOrderCreatedOrders_updatesExpiredStatus() {
        Order firstOrder = Order.builder()
                .orderStatus(OrderStatus.ORDER_CREATED)
                .build();
        Order secondOrder = Order.builder()
                .orderStatus(OrderStatus.ORDER_CREATED)
                .build();
        List<Order> expiredOrders = new ArrayList<>(List.of(firstOrder, secondOrder));

        when(orderRepository.findAllByOrderStatusAndCreatedAtBefore(eq(OrderStatus.ORDER_CREATED), any(LocalDateTime.class)))
                .thenReturn(expiredOrders);

        int expiredCount = orderService.expireOrderCreatedOrders();

        assertThat(expiredCount).isEqualTo(2);
        assertThat(firstOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDER_EXPIRED);
        assertThat(secondOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDER_EXPIRED);
        verify(orderRepository).saveAll(expiredOrders);
    }
}
