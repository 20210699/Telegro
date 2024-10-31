package com.telegro.telegro.domain.order.controller;

import com.telegro.telegro.domain.order.dto.response.OrderListDTO;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.service.OrderService;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs{
    private final OrderService orderService;
    private final HttpSession httpSession;

    @PostMapping("/create")
    public SuccessResponse<?> createOrder(Long id, List<Long> cartId) {
        Order temporaryOrder = orderService.createOrder(id, cartId);

        // 세션에 임시 주문 정보를 저장
        httpSession.setAttribute("temporaryOrder", temporaryOrder);
        httpSession.setAttribute("cartId", cartId); // 장바구니 id 저장

        Object cartIdsAttribute = httpSession.getAttribute("cartIds");

        return SuccessResponse.of(cartIdsAttribute);
    }

    @PostMapping("/done")
    public ResponseEntity<Object> completeOrder(@RequestBody OrderDto request) {

        OrderDto orders = modelMapper.map(request, OrderDto.class);

        // 세션에서 임시 주문 정보를 가져옴
        Orders temporaryOrder = (Orders) httpSession.getAttribute("temporaryOrder");

        if (temporaryOrder == null) {
            return ResponseEntity.badRequest().body("임시 주문 정보를 찾을 수 없습니다.");
        }

        Orders completedOrder = orderService.orderConfirm(temporaryOrder, orders);

        OrderResponseDto orderResponseDto = new OrderResponseDto(completedOrder);

        return ResponseEntity.ok(orderResponseDto);

    }

    @GetMapping
    public SuccessResponse<OrderListDTO> getOrders(Long id, LocalDate startDate, LocalDate endDate, int page, int size) {
        return null;
//        return SuccessResponse.of(orderService.getOrders(id, startDate, endDate, page, size));
    }
}