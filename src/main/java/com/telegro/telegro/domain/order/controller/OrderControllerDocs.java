package com.telegro.telegro.domain.order.controller;

import com.telegro.telegro.domain.notice.dto.response.NoticeDetailDTO;
import com.telegro.telegro.domain.order.dto.request.OrderRequestDTO;
import com.telegro.telegro.domain.order.dto.response.*;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderControllerDocs {

    @Operation(summary = "주문을 임시 등록합니다.", description = "장바구니에서 주문 클릭시 해당 장바구니들의 id를 리스트로 받는다. id로 필요 정보를 찾아 주문 테이블을 생성하고 이를 세션에 임시 저장한다.")
    @ApiResponse(responseCode = "200", description = "주문 임시 등록 성공")
    public SuccessResponse<temporaryOrderDTO> createOrder(@LoginInfo Long id, @RequestBody List<Long> cartId);

    @Operation(summary = "주문을 처리합니다.", description = "주문 정보 입력 후 결제하기를 누르면 주문 테이블이 저장된다.")
    @ApiResponse(responseCode = "200", description = "주문 처리 성공")
    public SuccessResponse<OrderResponseDTO> completeOrder(@LoginInfo Long id, @RequestBody OrderRequestDTO request);

    /*@Operation(summary = "주문 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공")
    public SuccessResponse<OrderListDTO> getOrders(@LoginInfo Long id,
                                                   @RequestParam(value = "startDate", required = false) LocalDate startDate,
                                                   @RequestParam(value = "endDate", required = false) LocalDate endDate,
                                                   @RequestParam(value = "page",defaultValue = "0") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size);*/

    @Operation(summary = "검색 조건에 따른 주문 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공")
    public SuccessResponse<OrderListDTO> getOrders(@LoginInfo Long id,
                                                   @RequestParam(value = "filterBy", required = false) @Parameter(description = "필터",
                                                           examples =
                                                                   {@ExampleObject(name = "상품명", value = "product"),
                                                                       @ExampleObject(name = "주문자명", value = "user")}) String filterBy,
                                                   @RequestParam(value = "q", required = false) String query,
                                                   @RequestParam(value = "startDate", required = false) LocalDate startDate,
                                                   @RequestParam(value = "endDate", required = false) LocalDate endDate,
                                                   @RequestParam(value = "cursorCreatedAt", required = false) LocalDateTime cursorCreatedAt,
                                                   @RequestParam(value = "cursorId", required = false) Long cursorId,
                                                   @RequestParam(value = "size", defaultValue = "10") int size);

    @Operation(summary = "주문의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공")
    public SuccessResponse<OrderDetailResponseDTO> getOrderDetail(@LoginInfo Long id, @PathVariable Long orderId);

    @Operation(summary = "주문 상태를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "주문 상태 변경 성공")
    public SuccessResponse<Boolean> updateOrderStatus(@LoginInfo Long id, @PathVariable Long orderId,
                                                      @RequestParam @Parameter(description = "변경할 주문 상태",
                                                              examples =
                                                                      {@ExampleObject(name = "주문 완료", value = "ORDER_COMPLETED"),
                                                                              @ExampleObject(name = "주문 취소", value = "ORDER_CANCELLED"),
                                                                              @ExampleObject(name = "배송 중", value = "SHIPPING"),
                                                                              @ExampleObject(name = "배송 완료", value = "DELIVERY_COMPLETED")}) OrderStatus status);
}
