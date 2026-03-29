package com.telegro.telegro.domain.order.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Builder
public record OrderListDTO(
        boolean isLast,
        LocalDateTime nextCursorCreatedAt,
        Long nextCursorId,
        BigDecimal totalPrice,
        List<OrderDetailDTO> orders
) {
}
