package com.telegro.telegro.domain.payment.repository;

import com.telegro.telegro.domain.payment.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentHistory, Long> {
}
