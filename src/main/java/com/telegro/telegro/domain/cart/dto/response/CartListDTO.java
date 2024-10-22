package com.telegro.telegro.domain.cart.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CartListDTO(
        boolean isLast,
        int totalPage,
        long totalElement,
        double totalPrice,
        List<CartResponseDTO> carts
) {
}
