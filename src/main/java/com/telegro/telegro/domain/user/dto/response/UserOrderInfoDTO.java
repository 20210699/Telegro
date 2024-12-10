package com.telegro.telegro.domain.user.dto.response;

import com.telegro.telegro.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserOrderInfoDTO(
        Long id,
        String username
) {
    public static UserOrderInfoDTO of(User user, String username) {
        return UserOrderInfoDTO.builder()
                .id(user.getId())
                .username(username)
                .build();
    }
}
