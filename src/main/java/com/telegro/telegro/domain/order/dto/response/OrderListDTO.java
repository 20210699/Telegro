package com.telegro.telegro.domain.order.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
@Builder
public record OrderListDTO(
        boolean isLast,
        int totalPage,
        long totalElement,
        BigDecimal totalPrice,
        List<OrderDetailDTO> orders
) {
}
