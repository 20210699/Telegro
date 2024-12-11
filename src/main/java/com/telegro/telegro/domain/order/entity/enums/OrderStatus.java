package com.telegro.telegro.domain.order.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상태를 나타내는 열거형")
public enum OrderStatus {
    @Schema(description = "주문이 완료된 상태")
    ORDER_COMPLETED,

    @Schema(description = "결제가 완료된 상태")
    PAYMENT_COMPLETED, // Todo : 결제 완료는 어떻게 확인? 웹훅 리다이렉트 url 수정

    @Schema(description = "주문이 취소된 상태")
    ORDER_CANCELLED,

    @Schema(description = "배송 중인 상태")
    SHIPPING,

    @Schema(description = "배송이 완료된 상태")
    DELIVERY_COMPLETED
}
