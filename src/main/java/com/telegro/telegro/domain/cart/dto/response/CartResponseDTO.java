package com.telegro.telegro.domain.cart.dto.response;

import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.product.entity.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartResponseDTO(
        @Schema(description = "장바구니 id")
        Long id,
        @Schema(description = "상품 대표이미지")
        String coverImage,
        @Schema(description = "상품 카테고리")
        Category productCategory,
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
        @Schema(description = "상품 전체 옵션")
        List<String> productOptions
) {

    public static CartResponseDTO of(Cart cart) {
        return CartResponseDTO.builder()
                .id(cart.getId())
                .coverImage(cart.getProduct().getCoverImage())
                .productCategory(cart.getProduct().getCategory())
                .productName(cart.getProduct().getProductName())
                .productModel(cart.getProduct().getProductModel())
                .selectOption(cart.getSelectOption())
                .inputOption(cart.getInputOption())
                .productPrice(cart.getPrice())
                .quantity(cart.getQuantity())
                .productOptions(cart.getProduct().getOptions())
                .build();
    }
}
