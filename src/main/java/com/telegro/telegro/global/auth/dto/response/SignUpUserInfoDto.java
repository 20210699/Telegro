package com.telegro.telegro.global.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
  private String address;
  private String addressDetail;
  private String zipCode;
}
