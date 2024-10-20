package com.telegro.telegro.domain.user.dto.response;

import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserDTO(
        @Schema(description = "회원 id")
        Long id,
        @Schema(description = "단가 적용")
        Role role,
        @Schema(description = "회원 이름")
        String userName,
        @Schema(description = "전화번호")
        String phone,
        @Schema(description = "이메일")
        String email,
        @Schema(description = "회원 아이디")
        String userId,
        @Schema(description = "가입일")
        LocalDateTime createdDate,
        @Schema(description = "총 주문액")
        Long totalPrice
) {
    public static UserDTO of(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .role(user.getRole())
                .userName(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .userId(user.getUserId())
                .createdDate(user.getCreatedAt())
//                .totalPrice() //Todo: totalPrice 구현
                .build();
    }
}
