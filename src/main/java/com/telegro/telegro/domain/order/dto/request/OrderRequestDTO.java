package com.telegro.telegro.domain.order.dto.request;

import com.telegro.telegro.domain.order.entity.enums.PaymentMethod;
import com.telegro.telegro.domain.user.entity.DeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderRequestDTO(
        @Schema(description = "배송 정보 : 배송지 이름, 전화번호, 주소, 우편번호, 상세 주소")
        DeliveryAddress deliveryAddress,
        @Schema(description = "요청 사항")
        String request,
        @Schema(description = "배송비")
        BigDecimal shoppingCost,
        @Schema(description = "사용할 적립금")
        BigDecimal pointsToUse,
        @Schema(description = "적립될 적립금")
        BigDecimal pointsToEarn,
        @Schema(description = "결제 방법")
        PaymentMethod paymentMethod
) {
}
