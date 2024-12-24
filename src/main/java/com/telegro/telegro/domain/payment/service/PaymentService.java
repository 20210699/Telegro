package com.telegro.telegro.domain.payment.service;

import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderRepository orderRepository;

    @Transactional
    public void updatePendingPaymentsToFailed() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        List<Order> pendingPayments = orderRepository.findByPaymentStatusAndUpdatedAtBefore(PaymentStatus.PENDING, threshold);

        for (Order order : pendingPayments) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.getUser().setPoint(order.getUser().getPoint()
                    .subtract(order.getPointsToEarn())
                    .add(order.getPointsToUse()));
        }

        orderRepository.saveAll(pendingPayments);
    }
}
