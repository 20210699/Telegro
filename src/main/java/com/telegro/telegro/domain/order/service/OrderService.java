package com.telegro.telegro.domain.order.service;

import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.repository.OrderRepository;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private OrderRepository orderRepository;

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

/*
    private String getProductNames(List<Cart> carts) {
        StringBuilder productNamesBuilder = new StringBuilder();
        for (Cart cart : carts) {

            String productName = cart.getProduct().getProductName();

            if (!productNamesBuilder.isEmpty()) {
                productNamesBuilder.append(", ");
            }
            productNamesBuilder.append(productName);
        }
        return productNamesBuilder.toString();
    }

    // 회원 전화번호를 가져오는 메서드
    private String getMemberPhoneNumber(List<Cart> carts) {
        Long userId = carts.get(0).getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));
        return (user != null && user.getPhone() != null) ? user.getPhone() : null;
    }

    // 총 가격을 계산하는 메서드
    private BigDecimal calculateTotalPrice(List<Cart> carts) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Cart cart : carts) {
            BigDecimal cartPrice = cart.getTotalPrice();
            totalPrice = totalPrice.add(cartPrice);
        }
        return totalPrice;
    }*/


    // 주문한 상품 목록
    /*public OrderListDTO getOrders(Long id, LocalDate startDate, LocalDate endDate, int page, int size) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));
        PageRequest pageRequest = PageRequest.of(page, size);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        Page<Order> orders = orderRepository.findByCreatedAtBetweenAndUser(startDateTime, endDateTime, user, pageRequest);

        boolean isLast = orders.isLast();
        int totalPage = orders.getTotalPages();
        long totalElement = orders.getTotalElements();

        // 카트에 담긴 상품 중에 주문한 거
        List<CartProductDTO> products;

        List<OrderDetailDTO> orderDTOs = orders.getContent().stream()
                .map(order -> OrderDetailDTO.of(order,products)).toList();

        return OrderListDTO.builder()
                .isLast(isLast)
                .totalPage(totalPage)
                .totalElement(totalElement)
                .orders(orderDTOs)
                .build();
    }*/

    private String generateMerchantUid() {
        // 현재 날짜와 시간을 포함한 고유한 문자열 생성
        String uniqueString = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDay = today.format(formatter).replace("-", "");

        // 무작위 문자열과 현재 날짜/시간을 조합하여 주문번호 생성
        return formattedDay +'-'+ uniqueString;
    }
}
