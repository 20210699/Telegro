package com.telegro.telegro.global.auth.dto.response;

import com.telegro.telegro.domain.user.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record LoginDto(
        @Schema(description = "user 등급")
        Role userRole,
        @Schema(description = "token 값")
        String accessToken
) {
}
