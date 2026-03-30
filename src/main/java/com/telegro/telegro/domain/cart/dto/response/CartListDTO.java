package com.telegro.telegro.domain.cart.dto.response;

import com.telegro.telegro.global.apiPayLoad.response.CursorPagedResponse;

import java.math.BigDecimal;
public record CartListDTO(
        BigDecimal totalPrice,
        CursorPagedResponse<CartResponseDTO> carts
) {
}
