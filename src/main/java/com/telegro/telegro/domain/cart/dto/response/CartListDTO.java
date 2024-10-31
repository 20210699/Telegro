package com.telegro.telegro.domain.cart.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartListDTO(
        boolean isLast,
        int totalPage,
        long totalElement,
        BigDecimal totalPrice,
        List<CartResponseDTO> carts
) {
}
