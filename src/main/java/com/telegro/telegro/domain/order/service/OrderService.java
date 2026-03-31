package com.telegro.telegro.domain.order.service;

import com.telegro.telegro.domain.cart.dto.response.CartProductDTO;
import com.telegro.telegro.domain.cart.dto.response.CartResponseDTO;
import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.entity.enums.CartStatus;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.order.dto.request.OrderRequestDTO;
import com.telegro.telegro.domain.order.dto.response.*;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.order.repository.OrderRepository;
import com.telegro.telegro.domain.user.dto.response.UserOrderInfoDTO;
import com.telegro.telegro.domain.user.entity.DeliveryAddress;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.DeliveryAddressRepository;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import com.telegro.telegro.global.apiPayLoad.response.CursorPagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;

    @Transactional
    public Order createOrder(Long id, List<Long> cartId) {
        List<Cart> carts = cartRepository.findByIdIn(cartId);

        Long userId = carts.get(0).getUser().getId();

        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        // 모든 장바구니의 userId가 동일한지 확인
        boolean sameUser = carts.stream()
                .allMatch(cart -> cart.getUser().getId().equals(userId));
        if (!sameUser || user == null) {
            // 동일하지 않거나 회원이 존재하지 않는 경우, 주문 생성 실패
            return null;
        }

        // 주문서 내용 중 사용자에게 입력받지 않고 자동으로 가져올 값 반환
        return new Order(user, carts);
    }

    @Transactional
    public temporaryOrderDTO getOrderInfo(Long id, List<Long> cartId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        List<Cart> carts = cartRepository.findByIdIn(cartId);

        List<CartProductDTO> products = carts.stream()
                .map(CartProductDTO::of).toList();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Cart cart : carts) {
            totalPrice = totalPrice.add(cart.getTotalPrice());
        }

        BigDecimal points = totalPrice.multiply(new BigDecimal("0.01")).setScale(0, RoundingMode.HALF_UP);

        return temporaryOrderDTO.builder()
                .cartProductDTOS(products)
                .userName(user.getUsername())
                .userEmail(user.getEmail())
                .totalPrice(totalPrice)
                .totalPoint(user.getPoint())
                .pointToEarn(points)
                .build();
    }

    @Transactional
    public OrderResponseDTO orderConfirm(Long id, Order temporaryOrder, OrderRequestDTO request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if(!user.getId().equals(temporaryOrder.getUser().getId())) {
            throw CustomException.of(Error.BAD_REQUEST_ERROR);
        }

        if(user.getPoint().compareTo(request.pointsToUse()) < 0){
            throw CustomException.of(Error.INSUFFICIENT_POINTS);
        }

        // 배송지 중복성 검사 후 중복하지 않으면 새로운 데이터로 저장
        DeliveryAddress deliveryAddress = request.deliveryAddress();
        DeliveryAddress savedAddress = deliveryAddressRepository
                .findByUserAndRecipientNameAndAddressAndAddressDetailAndZipcode(
                        user,
                        deliveryAddress.getRecipientName(),
                        deliveryAddress.getAddress(),
                        deliveryAddress.getAddressDetail(),
                        deliveryAddress.getZipcode()
                )
                .orElseGet(() -> deliveryAddressRepository.save(deliveryAddress));

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Cart cart : temporaryOrder.getCarts()) {
            totalPrice = totalPrice.add(cart.getTotalPrice());
            if (user.getCompany()!=null) {
                cart.setCartStatus(CartStatus.ORDERED);
            }
            cartRepository.save(cart);
        }

        totalPrice = totalPrice.subtract(request.pointsToUse()).add(request.shoppingCost());
        log.info("결제 해야하는 금액 : {}", totalPrice.toPlainString());

        PaymentStatus paymentStatus;
        OrderStatus orderStatus;

        if(user.getCompany() != null){
            paymentStatus = PaymentStatus.PENDING;
            orderStatus = OrderStatus.ORDER_COMPLETED;

            user.setTotalPrice(totalPrice.add(user.getTotalPrice()));
            user.setPoint(user.getPoint()
                    .subtract(request.pointsToUse())
                    .add(request.pointsToEarn()));

            log.info("사용자 point : {}", user.getPoint());
        } else {
            paymentStatus = PaymentStatus.FAILED;
            orderStatus = OrderStatus.ORDER_CREATED;
        }

        Order order = Order.builder()
                .orderStatus(orderStatus)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(paymentStatus)
                .pointsToUse(request.pointsToUse())
                .pointsToEarn(request.pointsToEarn())
                .amount(totalPrice)
                .shippingCost(request.shoppingCost())
                .request(request.request())
                .carts(temporaryOrder.getCarts())
                .user(temporaryOrder.getUser())
                .deliveryAddress(savedAddress)
                .build();

        List<CartResponseDTO> products = temporaryOrder.getCarts().stream()
                .map(CartResponseDTO::of).toList();

        Order savedOrder = orderRepository.save(order);

        return OrderResponseDTO.builder()
                .id(savedOrder.getId())
                .createdAt(savedOrder.getCreatedAt())
                .orderNumber(savedOrder.getOrderNumber())
                .products(products)
                .userName(savedOrder.getUser().getUsername())
                .userPhone(savedOrder.getUser().getPhone())
                .paymentMethod(savedOrder.getPaymentMethod())
                .usedPoint(request.pointsToUse())
                .shippingCost(savedOrder.getShippingCost())
                .totalPrice(savedOrder.getAmount())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderListDTO getOrders(Long id, String filteredBy, String query, LocalDate startDate, LocalDate endDate,
                                  OrderStatus orderStatus, LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if (size < 1) {
            throw CustomException.of(Error.BAD_REQUEST_ERROR);
        }

        PageRequest pageRequest = PageRequest.of(0, size + 1, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("createdAt"),
                org.springframework.data.domain.Sort.Order.desc("id")
        ));

        if ((cursorCreatedAt == null) != (cursorId == null)) {
            throw CustomException.of(Error.BAD_REQUEST_ERROR);
        }

        OrderSearchResult searchResult = searchOrders(user, filteredBy, query, startDate, endDate, orderStatus, cursorCreatedAt, cursorId, pageRequest);

        boolean isLast = searchResult.orders().size() <= size;
        List<Order> pagedOrders = isLast ? searchResult.orders() : new ArrayList<>(searchResult.orders().subList(0, size));
        Order nextCursorOrder = isLast ? null : pagedOrders.get(pagedOrders.size() - 1);
        List<OrderDetailDTO> orderDTOs = toOrderDetailDTOs(pagedOrders);

        return OrderListDTO.builder()
                .totalPrice(searchResult.totalPrice())
                .orders(CursorPagedResponse.of(
                        !isLast,
                        CursorPagedResponse.cursorOf(
                                nextCursorOrder != null ? nextCursorOrder.getId() : null,
                                nextCursorOrder != null ? nextCursorOrder.getCreatedAt() : null
                        ),
                        orderDTOs
                ))
                .build();
    }

    @Transactional(readOnly = true)
    public OrderFullListDTO getAllOrders(Long id, String filteredBy, String query, LocalDate startDate,
                                         LocalDate endDate, OrderStatus orderStatus) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        OrderSearchResult searchResult = searchOrders(user, filteredBy, query, startDate, endDate, orderStatus,
                null, null, Pageable.unpaged());

        return OrderFullListDTO.builder()
                .totalPrice(searchResult.totalPrice())
                .orders(toOrderDetailDTOs(searchResult.orders()))
                .build();
    }

    private OrderSearchResult searchOrders(User user, String filteredBy, String query, LocalDate startDate, LocalDate endDate,
                                           OrderStatus orderStatus, LocalDateTime cursorCreatedAt, Long cursorId, Pageable pageable) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        String normalizedQuery = (query == null || query.isBlank()) ? null : query;
        User targetUser = user.getRole().equals(Role.ADMIN) ? null : user;

        List<Order> orders;
        BigDecimal totalPrice;

        if ("product".equals(filteredBy)) {
            orders = orderRepository.findOrdersByProductAndQuery(startDateTime, endDateTime, targetUser,
                    normalizedQuery, orderStatus, cursorCreatedAt, cursorId, pageable);
            totalPrice = orderRepository.findTotalAmountByProductAndQuery(startDateTime, endDateTime, targetUser, normalizedQuery, orderStatus);
        } else if ("user".equals(filteredBy)) {
            orders = orderRepository.findOrdersByUserAndQuery(startDateTime, endDateTime, targetUser,
                    normalizedQuery, orderStatus, cursorCreatedAt, cursorId, pageable);
            totalPrice = orderRepository.findTotalAmountByUserAndQuery(startDateTime, endDateTime, targetUser, normalizedQuery, orderStatus);
        } else {
            orders = orderRepository.findOrdersByDateRangeAndUser(startDateTime, endDateTime, targetUser,
                    orderStatus, cursorCreatedAt, cursorId, pageable);
            totalPrice = orderRepository.findTotalAmountByDateRangeAndUser(startDateTime, endDateTime, targetUser, orderStatus);
        }

        return new OrderSearchResult(orders, totalPrice);
    }

    private List<OrderDetailDTO> toOrderDetailDTOs(List<Order> orders) {
        return orders.stream()
                .map(order -> {
                    List<CartProductDTO> products = order.getCarts().stream()
                            .map(CartProductDTO::of)
                            .toList();

                    String username;
                    if (order.getUser().getRole().equals(Role.MEMBER) || order.getUser().getRole().equals(Role.ADMIN)) {
                        username = order.getUser().getUsername();
                    } else {
                        username = order.getUser().getCompany().getCompanyName();
                    }
                    UserOrderInfoDTO userDTO = UserOrderInfoDTO.of(order.getUser(), username);

                    return OrderDetailDTO.of(order, products, userDTO);
                })
                .toList();
    }

    private record OrderSearchResult(
            List<Order> orders,
            BigDecimal totalPrice
    ) {
    }

    @Transactional
    public void updateOrderStatus(Long id, Long orderId, OrderStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if(!user.getRole().equals(Role.ADMIN)) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.of(Error.ORDER_NOT_FOUND));

        switch (status) {
            case DELIVERY_COMPLETED, SHIPPING -> order.setPaymentStatus(PaymentStatus.COMPLETED);
            default -> {
                log.error("잘못된 주문 상태 : {}", status);
                throw CustomException.of(Error.BAD_REQUEST_ERROR);
            }
        }
        order.setOrderStatus(status);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponseDTO getOrderDetail(Long id, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.of(Error.ORDER_NOT_FOUND));

        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if (!(user.getRole().equals(Role.ADMIN) || order.getUser().equals(user))) {
            throw CustomException.of(Error.INVALID_TOKEN_ERROR);
        }

        return OrderDetailResponseDTO.of(order);
    }
}
