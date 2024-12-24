package com.telegro.telegro.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentScheduler {
    private final PaymentService paymentService;

    @Scheduled(cron = "0 0 * * * ?") // 매 정시 실행
    public void schedulePendingStatusUpdate() {
        paymentService.updatePendingPaymentsToFailed();
    }
}
