package com.telegro.telegro.domain.order.service;

import com.telegro.telegro.domain.cart.dto.response.CartProductDTO;
import com.telegro.telegro.domain.cart.dto.response.CartResponseDTO;
import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.entity.enums.CartStatus;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.order.dto.request.OrderRequestDTO;
import com.telegro.telegro.domain.order.dto.response.OrderDetailDTO;
import com.telegro.telegro.domain.order.dto.response.OrderListDTO;
import com.telegro.telegro.domain.order.dto.response.OrderResponseDTO;
import com.telegro.telegro.domain.order.dto.response.temporaryOrderDTO;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.order.repository.OrderRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

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
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

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

    public temporaryOrderDTO getOrderInfo(Long id, List<Long> cartId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        List<Cart> carts = cartRepository.findByIdIn(cartId);

        List<CartProductDTO> products = carts.stream()
                .map(CartProductDTO::of).toList();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Cart cart : carts) {
            totalPrice = totalPrice.add(cart.getTotalPrice()); // add 메서드로 합산
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

    private String generateMerchantUid() {
        // 현재 날짜와 시간을 포함한 고유한 문자열 생성
        String uniqueString = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDay = today.format(formatter).replace("-", "");

        // 무작위 문자열과 현재 날짜/시간을 조합하여 주문번호 생성
        return formattedDay +'-'+ uniqueString;
    }

    @Transactional
    public OrderResponseDTO orderConfirm(Long id, Order temporaryOrder, OrderRequestDTO request) {
        String merchantUid = generateMerchantUid(); //주문번호 생성

        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!user.getId().equals(temporaryOrder.getUser().getId())) {
            log.error("User is not the same");
            throw CustomException.of(Error.BAD_REQUEST_ERROR);
        }

        if(user.getPoint().compareTo(request.pointsToUse()) < 0){
            throw CustomException.of(Error.INSUFFICIENT_POINTS);
        }

        // 배송지 중복성 검사 후 중복하지 않으면 새로운 데이터로 저장
        DeliveryAddress deliveryAddress = request.deliveryAddress();
        DeliveryAddress savedAddress = deliveryAddressRepository
                .findByUserAndAddressAndAddressDetailAndZipcode(
                        user,
                        deliveryAddress.getAddress(),
                        deliveryAddress.getAddressDetail(),
                        deliveryAddress.getZipcode()
                )
                .orElseGet(() -> deliveryAddressRepository.save(deliveryAddress));

        Order order = Order.builder()
                .orderNumber(merchantUid)
                .orderStatus(OrderStatus.ORDER_COMPLETED)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .shippingCost(request.shoppingCost())
                .request(request.request())
                .carts(temporaryOrder.getCarts())
                .user(temporaryOrder.getUser())
                .deliveryAddress(savedAddress)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Cart cart : temporaryOrder.getCarts()) {
            totalPrice = totalPrice.add(cart.getTotalPrice()); // add 메서드로 합산
            cart.setCartStatus(CartStatus.ORDERED); // Todo : 주문이 성공하면 CartStatus를 ORDERED로 수정
            cartRepository.save(cart);
        }

        user.setTotalPrice(totalPrice.add(user.getTotalPrice()));
        user.setPoint(user.getPoint()
                .subtract(request.pointsToUse())
                .add(request.pointsToEarn()));

        userRepository.save(user);

        List<CartResponseDTO> products = temporaryOrder.getCarts().stream()
                .map(CartResponseDTO::of).toList();

        Order savedOrder = orderRepository.save(order);

        return OrderResponseDTO.builder()
                .id(savedOrder.getId())
                .createdAt(savedOrder.getCreatedAt())
                .coverImage(savedOrder.getCarts().get(0).getProduct().getCoverImage())
                .orderNumber(savedOrder.getOrderNumber())
                .products(products)
                .userName(savedOrder.getUser().getUsername())
                .userPhone(savedOrder.getUser().getPhone())
                .deliveryAddress(savedOrder.getDeliveryAddress())
                .paymentMethod(savedOrder.getPaymentMethod())
                .usedPoint(request.pointsToUse())
                .shippingCost(savedOrder.getShippingCost())
                .totalPrice(totalPrice)
                .build();
    }


    // 주문한 상품 목록
    public OrderListDTO getOrders(Long id, LocalDate startDate, LocalDate endDate, int page, int size) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Order> orders;

        if(user.getRole().equals(Role.ADMIN)){
            orders = orderRepository.findAll(pageRequest);
        } else {
            if (startDate != null && endDate != null) {
                // startDate와 endDate가 모두 있는 경우: 두 날짜 사이의 값
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                orders = orderRepository.findByCreatedAtBetweenAndUser(startDateTime, endDateTime, user, pageRequest);

            } else if (startDate != null) {
                // startDate만 있는 경우: 해당 날짜부터 현재까지의 값
                LocalDateTime startDateTime = startDate.atStartOfDay();
                orders = orderRepository.findByCreatedAtAfterAndUser(startDateTime, user, pageRequest);

            } else if (endDate != null) {
                // endDate만 있는 경우: 처음부터 해당 날짜까지의 값
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                orders = orderRepository.findByCreatedAtBeforeAndUser(endDateTime, user, pageRequest);

            } else {
                // startDate와 endDate가 모두 없는 경우: 전체 값
                orders = orderRepository.findByUser(user, pageRequest);
            }
        }

        boolean isLast = orders.isLast();
        int totalPage = orders.getTotalPages();
        long totalElement = orders.getTotalElements();

        // 카트에 담긴 상품 중에 주문한 거
        List<CartProductDTO> products = cartRepository.findAllOrderedByUser(user).stream().map(CartProductDTO::of).toList();

        List<OrderDetailDTO> orderDTOs = orders.getContent().stream()
                .map(order -> OrderDetailDTO.of(order, products)).toList();

        return OrderListDTO.builder()
                .isLast(isLast)
                .totalPage(totalPage)
                .totalElement(totalElement)
                .orders(orderDTOs)
                .build();
    }


    public void updateOrderStatus(Long id, Long orderId, OrderStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!user.getRole().equals(Role.ADMIN)) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        order.setOrderStatus(status);
        orderRepository.save(order);
    }
}
