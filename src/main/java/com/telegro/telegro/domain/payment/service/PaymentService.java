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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PaymentService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;

    public void processPaymentDone(Long id, PaymentRequestDto request) {

        Long orderId = request.getOrderId();
        Long totalPrice = request.getPrice();

        Order currentOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.of(Error.ORDER_NOT_FOUND));

        currentOrder.setPaymentStatus(PaymentStatus.COMPLETED);

        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        createPaymentHistory(currentOrder, user, totalPrice);

    }

    private void createPaymentHistory(Order order, User user, Long totalPrice) {
        List<Long> cartIdList = order.getCarts().stream().map(Cart::getId).toList();
        log.info("cartIdList: {}", cartIdList);
        for (Long cartId : cartIdList) {

            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> CustomException.of(Error.CART_NOT_FOUND));

            Product product = cart.getProduct();
            String option = cart.getInputOption() + ", " + cart.getSelectOption();

            PaymentHistory paymentHistory = new PaymentHistory(user, order, product, product.getProductName(), option, cart.getPrice(), totalPrice);

            paymentRepository.save(paymentHistory);
        }
    }
}
