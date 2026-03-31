package com.telegro.telegro.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
@Getter
@Builder
public record OrderFullListDTO(
        BigDecimal totalPrice,
        List<OrderDetailDTO> orders
) {
}
