package com.telegro.telegro.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CartRequestDTO (
        @Schema(description = "상품 옵션")
        String productOption,
        @Schema(description = "상품 수량")
        int quantity
){
}
