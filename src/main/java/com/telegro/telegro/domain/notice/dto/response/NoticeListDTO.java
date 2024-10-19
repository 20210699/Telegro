package com.telegro.telegro.domain.notice.dto.response;

import com.telegro.telegro.domain.cart.dto.response.CartResponseDTO;
import lombok.Builder;

import java.util.List;
@Builder
public record NoticeListDTO(
        boolean isLast,
        int totalPage,
        long totalElement,
        List<NoticeDTO> notices
) {
}
