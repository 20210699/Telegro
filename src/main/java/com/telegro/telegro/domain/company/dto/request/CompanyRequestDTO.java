package com.telegro.telegro.domain.company.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CompanyRequestDTO(
        @Schema(description = "로그인 시 입력하는 아이디")
        String userId,
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
        String companyItem
) {
}
