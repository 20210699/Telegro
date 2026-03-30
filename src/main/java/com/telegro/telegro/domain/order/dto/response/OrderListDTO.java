package com.telegro.telegro.domain.order.dto.response;

import com.telegro.telegro.global.apiPayLoad.response.CursorPagedResponse;

import java.math.BigDecimal;
public record OrderListDTO(
        BigDecimal totalPrice,
        CursorPagedResponse<OrderDetailDTO> orders
) {
}
