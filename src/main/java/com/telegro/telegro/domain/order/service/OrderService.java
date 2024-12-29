package com.telegro.telegro.domain.order.service;

import com.telegro.telegro.domain.cart.dto.response.CartProductDTO;
import com.telegro.telegro.domain.cart.dto.response.CartResponseDTO;
import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.entity.enums.CartStatus;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.company.repository.CompanyRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        if(user.getCompany() != null) {
            savedOrder.getUser().setTotalPrice(savedOrder.getAmount().add(order.getUser().getTotalPrice()));
            savedOrder.getUser().setPoint(savedOrder.getUser().getPoint()
                    .subtract(savedOrder.getPointsToUse())
                    .add(savedOrder.getPointsToEarn()));
        }

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
    public OrderListDTO getOrders(Long id, String filteredBy, String query, LocalDate startDate, LocalDate endDate, int page, int size) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        Page<Order> orders;
        BigDecimal totalPrice;

        if ("product".equals(filteredBy)) {
            orders = orderRepository.findOrdersByProductAndQuery(startDateTime, endDateTime, user.getRole().equals(Role.ADMIN) ? null : user, query, pageRequest);
            totalPrice = orderRepository.findTotalAmountByProductAndQuery(startDateTime, endDateTime, user.getRole().equals(Role.ADMIN) ? null : user, query);
        } else if ("user".equals(filteredBy)) {
            orders = orderRepository.findOrdersByUserAndQuery(startDateTime, endDateTime, user.getRole().equals(Role.ADMIN) ? null : user, query, pageRequest);
            totalPrice = orderRepository.findTotalAmountByUserAndQuery(startDateTime, endDateTime, user.getRole().equals(Role.ADMIN) ? null : user, query);
        } else {
            orders = orderRepository.findOrdersByDateRangeAndUser(startDateTime, endDateTime, user.getRole().equals(Role.ADMIN) ? null : user, pageRequest);
            totalPrice = orderRepository.findTotalAmountByDateRangeAndUser(startDateTime, endDateTime, user.getRole().equals(Role.ADMIN) ? null : user);
        }

        boolean isLast = orders.isLast();
        int totalPage = orders.getTotalPages();
        long totalElement = orders.getTotalElements();

        List<OrderDetailDTO> orderDTOs = orders.getContent().stream()
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

        return OrderListDTO.builder()
                .isLast(isLast)
                .totalPage(totalPage)
                .totalElement(totalElement)
                .totalPrice(totalPrice)
                .orders(orderDTOs)
                .build();
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