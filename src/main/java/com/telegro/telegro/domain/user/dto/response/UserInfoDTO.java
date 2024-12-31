package com.telegro.telegro.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record UserInfoDTO(
        @Schema(description = "회원 id")
        Long id,
        @Schema(description = "회원 이름")
        String userName,
        @Schema(description = "회원 아이디")
        String userId,
        @Schema(description = "전화번호")
        String phone,
        @Schema(description = "이메일")
        String email,
        @Schema(description = "적립금")
        BigDecimal point,
        @Schema(description = "배송지 목록")
        List<DeliveryAddressDetailDTO> addressList
) {
}
