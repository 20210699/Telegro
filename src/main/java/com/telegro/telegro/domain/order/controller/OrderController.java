package com.telegro.telegro.domain.order.controller;

import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.order.dto.request.OrderRequestDTO;
import com.telegro.telegro.domain.order.dto.response.OrderDetailResponseDTO;
import com.telegro.telegro.domain.order.dto.response.OrderListDTO;
import com.telegro.telegro.domain.order.dto.response.OrderResponseDTO;
import com.telegro.telegro.domain.order.dto.response.temporaryOrderDTO;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.service.OrderService;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs{
    private final OrderService orderService;
    private final HttpSession httpSession;

    @PostMapping("/create")
    public SuccessResponse<temporaryOrderDTO> createOrder(Long id, List<Long> cartId) {
        Order temporaryOrder = orderService.createOrder(id, cartId);

        httpSession.setAttribute("temporaryOrder", temporaryOrder);
        httpSession.setAttribute("cartId", cartId);

        return SuccessResponse.of(orderService.getOrderInfo(id, cartId));
    }

    @PostMapping("/done")
    public SuccessResponse<OrderResponseDTO> completeOrder(Long id, OrderRequestDTO request) {

        Order temporaryOrder = (Order) httpSession.getAttribute("temporaryOrder");

        if (temporaryOrder == null) {
            throw CustomException.of(Error.ORDER_NOT_FOUND);
        }

        OrderResponseDTO order = orderService.orderConfirm(id, temporaryOrder, request);
        log.info("orderConfirm, Cart IDs: {}", temporaryOrder.getCarts().stream().map(Cart::getId).toList());

        httpSession.removeAttribute("temporaryOrder");
        httpSession.removeAttribute("cartId");

        return SuccessResponse.of(order);
    }

    @GetMapping
    public SuccessResponse<OrderListDTO> getOrders(Long id, String filteredBy, String query, LocalDate startDate, LocalDate endDate,
                                                   LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        return SuccessResponse.of(orderService.getOrders(id, filteredBy, query, startDate, endDate, cursorCreatedAt, cursorId, size));
    }

    @GetMapping("/{orderId}")
    public SuccessResponse<OrderDetailResponseDTO> getOrderDetail(Long id, Long orderId) {
        return SuccessResponse.of(orderService.getOrderDetail(id, orderId));
    }

    @PatchMapping("/{orderId}")
    public SuccessResponse<Boolean> updateOrderStatus(Long id, Long orderId, OrderStatus status) {
        orderService.updateOrderStatus(id, orderId, status);
        return SuccessResponse.of();
    }
}
