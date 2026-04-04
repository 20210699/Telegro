package com.telegro.telegro.domain.order.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상태를 나타내는 열거형")
public enum OrderStatus {
    @Schema(description = "주문이 생성된 상태")
    ORDER_CREATED,

    @Schema(description = "주문이 만료된 상태")
    ORDER_EXPIRED,

    @Schema(description = "주문이 접수된 상태")
    ORDER_COMPLETED,

    @Schema(description = "결제가 완료된 상태")
    PAYMENT_COMPLETED,

    @Schema(description = "주문이 취소된 상태")
    ORDER_CANCELLED,

    @Schema(description = "배송 중인 상태")
    SHIPPING,

    @Schema(description = "배송이 완료된 상태")
    DELIVERY_COMPLETED
}
