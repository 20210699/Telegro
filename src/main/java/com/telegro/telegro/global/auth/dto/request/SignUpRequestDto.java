package com.telegro.telegro.global.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpRequestDto(
	@Schema(description = "회원 아이디")
	String userid,
	@Schema(description = "닉네임")
	String username,
	@Schema(description = "비밀번호")
	String password,
	@Schema(description = "전화번호")
	String phone,
	@Schema(description = "email")
	String email,
	@Schema(description = "주소")
	String address,
	@Schema(description = "상세 주소")
	String addressDetail,
	@Schema(description = "우편 번호")
	String zipCode
) {
}
