package com.telegro.telegro.domain.order.controller;

import com.telegro.telegro.domain.order.dto.request.OrderRequestDTO;
import com.telegro.telegro.domain.order.dto.response.OrderListDTO;
import com.telegro.telegro.domain.order.dto.response.OrderResponseDTO;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

public interface OrderControllerDocs {
    @Operation(summary = "주문을 임시 등록합니다.", description = "장바구니에서 주문 클릭시 해당 장바구니들의 id를 리스트로 받는다. id로 필요 정보를 찾아 주문 테이블을 생성하고 이를 세션에 임시 저장한다.")
    @ApiResponse(responseCode = "200", description = "주문 임시 등록 성공")
    public SuccessResponse<?> createOrder(@LoginInfo Long id, @RequestBody List<Long> cartId);

    @Operation(summary = "주문을 처리합니다.", description = "주문 정보 입력 후 결제하기를 누르면 주문 테이블이 저장된다.")
    @ApiResponse(responseCode = "200", description = "주문 처리 성공")
    public SuccessResponse<OrderResponseDTO> completeOrder(@LoginInfo Long id, @RequestBody OrderRequestDTO request);

    @Operation(summary = "주문 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공")
    public SuccessResponse<OrderListDTO> getOrders(@LoginInfo Long id,
                                                   @RequestParam(value = "startDate", required = false) LocalDate startDate,
                                                   @RequestParam(value = "endDate", required = false) LocalDate endDate,
                                                   @RequestParam(value = "page",defaultValue = "0") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size);
}
