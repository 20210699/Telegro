package com.telegro.telegro.domain.product.dto.response;

import com.telegro.telegro.domain.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ProductResponseDTO (
        @Schema(description = "상품 id")
        Long id,
        @Schema(description = "모델명")
        String productModel,
        @Schema(description = "상품명")
        String productName,
        @Schema(description = "가격")
        String price,
        @Schema(description = "대표 이미지")
        String coverImage
){
        public static ProductResponseDTO of(Product product, String price) {
                return ProductResponseDTO.builder()
                        .id(product.getId())
                        .productModel(product.getProductModel())
                        .productName(product.getProductName())
                        .price(price)
                        .coverImage(product.getCoverImage())
                        .build();
        }
}
