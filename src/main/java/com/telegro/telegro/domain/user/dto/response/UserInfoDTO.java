package com.telegro.telegro.domain.user.dto.response;

import com.telegro.telegro.domain.user.entity.DeliveryAddress;
import com.telegro.telegro.domain.user.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
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
        @Schema(description = "배송지 목록")
        List<DeliveryAddress> addressList
) {
}
