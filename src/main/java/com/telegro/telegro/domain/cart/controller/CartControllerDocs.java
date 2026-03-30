package com.telegro.telegro.domain.cart.controller;

import com.telegro.telegro.domain.cart.dto.request.CartRequestDTO;
import com.telegro.telegro.domain.cart.dto.response.CartListDTO;
import com.telegro.telegro.domain.cart.dto.response.CreatedCartDTO;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

public interface CartControllerDocs {
    @Operation(description = "장바구니에 상품을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 담기 성공")
    public SuccessResponse<CreatedCartDTO> addCartItem(@LoginInfo Long id, @PathVariable Long productId, @RequestBody CartRequestDTO request);

    @Operation(description = "장바구니에 상품을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 조회 성공")
    public SuccessResponse<CartListDTO> getCartItems(
            @LoginInfo Long id,
            @RequestParam(value = "cursorCreatedAt", required = false) LocalDateTime cursorCreatedAt,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "size", defaultValue = "10") int size);

    @Operation(description = "장바구니에 상품을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 삭제 성공")
    public SuccessResponse<Boolean> deleteCartItem(@LoginInfo Long id, @PathVariable Long cartId);

    @Operation(description = "장바구니에 상품의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "장바구니 상품 정보 수정 성공")
    public SuccessResponse<CreatedCartDTO> updateCartItem(@LoginInfo Long id, @PathVariable Long cartId, @RequestBody CartRequestDTO request);
}
