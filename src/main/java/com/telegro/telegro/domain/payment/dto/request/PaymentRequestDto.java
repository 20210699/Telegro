package com.telegro.telegro.domain.payment.dto.request;

import lombok.Getter;

import java.util.List;
@Getter
public class PaymentRequestDto {
    Long orderId;
    Long price;
    List<Long> cartIds;
}
