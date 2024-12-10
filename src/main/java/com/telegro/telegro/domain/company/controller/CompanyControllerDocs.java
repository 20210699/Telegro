package com.telegro.telegro.domain.company.controller;

import com.telegro.telegro.domain.company.dto.request.CompanySignUpDTO;
import com.telegro.telegro.domain.company.dto.response.CompanyDetailDTO;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface CompanyControllerDocs {
    @Operation(summary = "공급 업체를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "공급 업체 등록 성공")
    public SuccessResponse<?> createCompany(@LoginInfo Long id, @RequestBody CompanySignUpDTO companySignUpDTO);

    @Operation(summary = "공급 업체를 상세 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공급 업체 상세 조회 성공")
    public SuccessResponse<CompanyDetailDTO> getCompanyDetail(@LoginInfo Long id);

    @Operation(summary = "공급 업체를 삭제합니다.")
    public SuccessResponse<?> deleteCompany(@LoginInfo Long id, @PathVariable Long userId);
}
