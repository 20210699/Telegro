package com.telegro.telegro.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CreateAddressDTO(
        @Schema(description = "배송지 id")
        Long id
) {
}
