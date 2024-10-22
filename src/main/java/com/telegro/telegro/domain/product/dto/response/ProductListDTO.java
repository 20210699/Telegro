package com.telegro.telegro.domain.product.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductListDTO(
        boolean isLast,
        int totalPage,
        long totalElement,
        List<ProductResponseDTO> products
) {
}
