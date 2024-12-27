package com.telegro.telegro.domain.product.controller;

import com.telegro.telegro.domain.product.dto.request.ProductRequestDTO;
import com.telegro.telegro.domain.product.dto.response.CreatedProductDTO;
import com.telegro.telegro.domain.product.dto.response.ProductDetailResponseDTO;
import com.telegro.telegro.domain.product.dto.response.ProductListDTO;
import com.telegro.telegro.domain.product.entity.enums.Category;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public interface ProductControllerDocs {
    @Operation(description = "상품을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "상품 등록 성공")
    public SuccessResponse<CreatedProductDTO> createProduct(@LoginInfo Long id, @RequestBody ProductRequestDTO request);

    @Operation(description = "상품 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    public SuccessResponse<ProductListDTO> getProducts(
            @LoginInfo Long id,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "category") Category category,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size);

    @Operation(description = "상품을 상세 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상품 조회 성공")
    public SuccessResponse<ProductDetailResponseDTO> getProductDetail(@LoginInfo Long id,@PathVariable Long productId);

    @Operation(summary = "상품을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "상품 삭제 성공")
    public SuccessResponse<Boolean> deleteProduct(
            @LoginInfo Long id,
            @PathVariable Long productId);

    @Operation(summary = "상품을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "상품 수정 성공")
    public SuccessResponse<ProductDetailResponseDTO> updateProduct(
            @LoginInfo Long id,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> request);
}
