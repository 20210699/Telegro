package com.telegro.telegro.domain.company.dto.response;

import com.telegro.telegro.domain.user.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CompanyDetailDTO(
        @Schema(description = "회원 id")
        Long id,
        @Schema(description = "단가 적용")
        Role role,
        @Schema(description = "회원 아이디")
        String userid,
        @Schema(description = "회원 이름")
        String username,
        @Schema(description = "전화번호")
        String phone,
        @Schema(description = "email")
        String email,
        @Schema(description = "주소")
        String address,
        @Schema(description = "상세 주소")
        String addressDetail,
        @Schema(description = "우편 번호")
        String zipCode,
        @Schema(description = "담당자 이름")
        String managerName,
        @Schema(description = "담당자 연락처")
        String managerPhone,
        @Schema(description = "상호")
        String companyName,
        @Schema(description = "사업자 번호")
        String companyNumber,
        @Schema(description = "업태")
        String companyType,
        @Schema(description = "종목")
        String companyItem,
        @Schema(description = "기타 문구 기재")
        String companyDescription
) {
}
