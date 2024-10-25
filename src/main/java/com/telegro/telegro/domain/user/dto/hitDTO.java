package com.telegro.telegro.domain.user.dto;

import lombok.Builder;

@Builder
public record hitDTO(
        String name,
        Integer hit,
        double percentage
) {
}
