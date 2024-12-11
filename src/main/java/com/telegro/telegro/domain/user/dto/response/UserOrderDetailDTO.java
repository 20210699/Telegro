package com.telegro.telegro.domain.user.dto.response;

import com.telegro.telegro.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UserOrderDetailDTO(
        @Schema(description = "주문자명(상호명)")
        String name,
        @Schema(description = "주문자 연락처(상호 전화번호)")
        String phone,
        @Schema(description = "담당자 연락처")
        String managerPhone,
        @Schema(description = "이메일")
        String email
) {
        public static UserOrderDetailDTO of(User user) {
                return UserOrderDetailDTO.builder()
                        .name(user.getUsername())
                        .phone(user.getPhone())
                        .managerPhone(user.getPhone()) // Todo : 추후 회사 조회 로직 추가
                        .email(user.getEmail())
                        .build();
        }
}
