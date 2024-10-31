package com.telegro.telegro.domain.order.dto.response;

import lombok.Builder;

import java.util.List;
@Builder
public record OrderListDTO(
        boolean isLast,
        int totalPage,
        long totalElement,
        List<OrderDetailDTO> orders
) {
}
