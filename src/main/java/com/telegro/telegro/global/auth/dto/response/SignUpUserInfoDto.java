package com.telegro.telegro.global.auth.dto.response;

import com.telegro.telegro.domain.user.entity.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpUserInfoDto {
  private String userid;
  private String username;
  private String password;
  private String phone;
  private String email;
  private Role role;
  private String address;
  private String addressDetail;
  private String zipCode;
}
