package com.telegro.telegro.domain.order.dto.response;

import com.telegro.telegro.domain.cart.dto.response.CartResponseDTO;
import com.telegro.telegro.domain.order.entity.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponseDTO(
        @Schema(description = "주문 id")
        Long id,
        @Schema(description = "주문 날짜")
        LocalDateTime createdAt,
        @Schema(description = "주문 번호")
        String orderNumber,
        @Schema(description = "상품 List : 상품 대표 이미지 , 상품명, 모델명, 수량, 결제 금액")
        List<CartResponseDTO> products,
        @Schema(description = "주문자 이름")
        String userName,
        @Schema(description = "주문자 연락처")
        String userPhone,
        @Schema(description = "결제 방법")
        PaymentMethod paymentMethod,
        @Schema(description = "할인 금액(적립금 사용 금액)")
        BigDecimal usedPoint,
        @Schema(description = "배송비")
        BigDecimal shippingCost,
        @Schema(description = "총 결제 금액")
        BigDecimal totalPrice
) {
}
