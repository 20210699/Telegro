package com.telegro.telegro.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
@Builder
public record HitListDTO(
        @Schema(description = "hitList")
        List<hitDTO> hits,
        @Schema(description = "평균")
        double averageHit,
        @Schema(description = "총 계")
        double totalHit,
        @Schema(description = "누적 총 계")
        double overAllTotalHit
) {
}
