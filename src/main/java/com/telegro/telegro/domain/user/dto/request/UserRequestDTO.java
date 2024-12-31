package com.telegro.telegro.domain.user.dto.request;

import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class UserRequestDTO {
    User user;
    Company company;
}
