package com.telegro.telegro.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderScheduler {
    private final OrderService orderService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleOrderExpiration() {
        orderService.expireOrderCreatedOrders();
    }
}
