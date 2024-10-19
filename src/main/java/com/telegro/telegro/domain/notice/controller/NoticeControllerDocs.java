package com.telegro.telegro.domain.notice.controller;

import com.telegro.telegro.domain.cart.dto.request.CartRequestDTO;
import com.telegro.telegro.domain.cart.dto.response.CreatedCartDTO;
import com.telegro.telegro.domain.notice.dto.response.CreatedNoticeDTO;
import com.telegro.telegro.domain.notice.entity.Notice;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface NoticeControllerDocs {
    @Operation(description = "장바구니에 상품을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 담기 성공")
    public SuccessResponse<CreatedNoticeDTO> createNotice(@LoginInfo Long id, @RequestBody Notice request);
}
