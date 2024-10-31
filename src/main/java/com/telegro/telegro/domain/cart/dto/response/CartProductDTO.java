package com.telegro.telegro.domain.cart.dto.response;

import com.telegro.telegro.domain.cart.entity.Cart;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CartProductDTO(
        @Schema(description = "상품 id")
        Long id,
//        @Schema(description = "상품 대표이미지")
//        String coverImage,
//        @Schema(description = "상품 카테고리")
//        Category productCategory,
        @Schema(description = "상품명")
        String productName,
        @Schema(description = "모델명")
        String productModel,
        @Schema(description = "선택한 옵션")
        String selectOption,
        @Schema(description = "기재한 옵션")
        String inputOption,
        @Schema(description = "상품 금액")
        BigDecimal productPrice,
        @Schema(description = "주문 수량")
        int quantity,
        @Schema(description = "수량 * 상품 가격")
        BigDecimal totalPrice
) {
    public static CartProductDTO of(Cart cart, BigDecimal productPrice) {
        return CartProductDTO.builder()
                .id(cart.getProduct().getId())
                .productName(cart.getProduct().getProductName())
                .productModel(cart.getProduct().getProductModel())
                .selectOption(cart.getSelectOption())
                .inputOption(cart.getInputOption())
                .productPrice(productPrice)
                .quantity(cart.getQuantity())
                .totalPrice(productPrice.multiply(BigDecimal.valueOf(cart.getQuantity())))
                .build();
    }
}
