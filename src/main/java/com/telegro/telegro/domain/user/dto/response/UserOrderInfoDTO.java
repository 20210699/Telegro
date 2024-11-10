package com.telegro.telegro.domain.user.dto.response;

import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.company.repository.CompanyRepository;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

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
