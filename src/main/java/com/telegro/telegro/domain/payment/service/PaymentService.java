package com.telegro.telegro.domain.payment.service;

import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.order.repository.OrderRepository;
import com.telegro.telegro.domain.payment.dto.request.PaymentRequestDto;
import com.telegro.telegro.domain.payment.entity.PaymentHistory;
import com.telegro.telegro.domain.payment.repository.PaymentRepository;
import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PaymentService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;

    public void processPaymentDone(Long id,PaymentRequestDto request) {

        Long orderId = request.getOrderId();
        Long totalPrice = request.getPrice();
        List<Long> cartIds = request.getCartIds();

        //orders 테이블에서 해당 부분 결제true 처리
        Order currentOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));
        currentOrder.setPaymentStatus(PaymentStatus.COMPLETED);

        // PaymentHistory 테이블에 저장할 Member 객체
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        // PaymentHistory 테이블에 저장할 Orders 객체
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("해당 주문서를 찾을 수 없습니다. Id : " + orderId));

        // 주문한 상품들에 대해 각각 결제내역 저장
        createPaymentHistory(cartIds, order, user, totalPrice);

    }

    // 결제내역 테이블 저장하는 메서드
    private void createPaymentHistory(List<Long> productMgtIdList, Order order, User user, Long totalPrice) {
        for (Long productMgtId : productMgtIdList) {

            Cart cart = cartRepository.findById(productMgtId)
                    .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

            Product product = cart.getProduct();
            String option = cart.getInputOption() + ", " + cart.getSelectOption(); // 상품옵션 문자열로 저장

            PaymentHistory paymentHistory = new PaymentHistory(user, order, product, product.getProductName(), option, cart.getPrice(), totalPrice);

            paymentRepository.save(paymentHistory);

        }
    }
}
