package com.telegro.telegro.domain.user.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserListDTO(
        boolean isLast,
        int totalPage,
        long totalElement,
        List<UserDTO> users
) {
}
