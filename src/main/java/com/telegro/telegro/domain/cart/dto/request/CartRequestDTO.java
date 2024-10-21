package com.telegro.telegro.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CartRequestDTO (
        @Schema(description = "선택한 상품 옵션")
        String selectOption,
        @Schema(description = "기재한 옵션")
        String inputOption,
        @Schema(description = "상품 수량")
        int quantity
){
}
