package com.telegro.telegro.domain.cart.controller;

import com.telegro.telegro.domain.cart.dto.request.CartRequestDTO;
import com.telegro.telegro.domain.cart.dto.response.CreatedCartDTO;
import com.telegro.telegro.domain.product.dto.request.ProductRequestDTO;
import com.telegro.telegro.domain.product.dto.response.CreatedProductDTO;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface CartControllerDocs {
    @Operation(description = "장바구니에 상품을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 담기 성공")
    public SuccessResponse<CreatedCartDTO> addCartItem(@LoginInfo Long id, @PathVariable Long productId, @RequestBody CartRequestDTO request);
}
