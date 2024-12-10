package com.telegro.telegro.domain.order.dto.response;

import com.telegro.telegro.domain.cart.dto.response.CartProductDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record temporaryOrderDTO(
        @Schema(description = "주문 상품 정보 : 대표 이미지, 상품명, 모델명, 개당 가격")
        List<CartProductDTO> cartProductDTOS,
        @Schema(description = "주문자 이름")
        String userName,
        @Schema(description = "주문자 이메일")
        String userEmail,
        @Schema(description = "상품 전체 가격")
        BigDecimal totalPrice,
        @Schema(description = "전체 적립금")
        BigDecimal totalPoint,
        @Schema(description = "적립 예정 포인트")
        BigDecimal pointToEarn
) {
}
