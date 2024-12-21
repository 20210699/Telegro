package com.telegro.telegro.domain.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.Payment;
import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.entity.enums.CartStatus;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.order.repository.OrderRepository;
import com.telegro.telegro.domain.payment.dto.request.WebhookDTO;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class PaymentController implements PaymentControllerDocs{
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private IamportClient iamportClient;
    private final ObjectMapper mapper;

    @Value("${imp.api.apikey}")
    private String apiKey;

    @Value("${imp.api.secretkey}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    // Todo : 결제 검증 엔드포인트 다시 만들기..

    @PostMapping("api/payments/cancel/{orderId}")
    public SuccessResponse<?> cancelPayment(Long id, Long orderId) throws IamportResponseException, IOException {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.of(Error.ORDER_NOT_FOUND));

        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if(!(user.getRole().equals(Role.ADMIN) || user.equals(order.getUser()))) {
            throw CustomException.of(Error.BAD_REQUEST_ERROR);
        }

        CancelData cancelData = new CancelData(order.getOrderNumber(), true);

        iamportClient.cancelPaymentByImpUid(cancelData);

        return SuccessResponse.of();
    }

    @Transactional
    @PostMapping("/payments/update")
    public SuccessResponse<?> updatePaymentStatus(WebhookDTO request) throws IamportResponseException, IOException {

        Payment payment = iamportClient.paymentByImpUid(request.getImp_uid()).getResponse();

        Order order = orderRepository.findByOrderNumber(request.getImp_uid())
                .orElseGet(() -> {
                    try {
                        var customData = mapper.readValue(payment.getCustomData(), Map.class);
                        Long orderId = Long.valueOf(customData.get("orderId").toString());
                        log.info("Parsed orderId: {}", orderId);

                        Order foundOrder = orderRepository.findById(orderId)
                                .orElseThrow(() -> CustomException.of(Error.ORDER_NOT_FOUND));

                        foundOrder.setOrderNumber(request.getImp_uid());
                        foundOrder.setReceiptUrl(payment.getReceiptUrl());
                        foundOrder.setTotalPrice(payment.getAmount()); // Todo : 가상 계좌 테스트 후 로직 결정

                        for (Cart cart : foundOrder.getCarts()) {
                            cart.setCartStatus(CartStatus.ORDERED);
                        }

                        return foundOrder;
                    } catch (JsonProcessingException e) {
                        log.error("JSON 파싱 오류 발생: {}", payment.getCustomData(), e);
                        throw CustomException.of(Error.INTERNAL_SERVER_ERROR);
                    }
                });

        if (request.getStatus() == null) {
            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            order.setPaymentStatus(PaymentStatus.FAILED);

            return SuccessResponse.of();
        }

        if (Objects.equals(request.getStatus(), payment.getStatus())) {
            switch (payment.getStatus()) {
                case "paid" -> {
                    order.setOrderStatus(OrderStatus.PAYMENT_COMPLETED);
                    order.setPaymentStatus(PaymentStatus.COMPLETED);

                    order.getUser().setTotalPrice(order.getTotalPrice().add(order.getUser().getTotalPrice()));
                    order.getUser().setPoint(order.getUser().getPoint()
                            .subtract(order.getPointsToUse())
                            .add(order.getPointsToEarn()));
                }
                case "ready" -> {
                    order.setOrderStatus(OrderStatus.ORDER_COMPLETED);
                    order.setPaymentStatus(PaymentStatus.PENDING);

                    order.getUser().setTotalPrice(order.getTotalPrice().add(order.getUser().getTotalPrice()));
                    order.getUser().setPoint(order.getUser().getPoint()
                            .subtract(order.getPointsToUse())
                            .add(order.getPointsToEarn()));
                }
                case "cancelled" -> {
                    order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
                    order.setPaymentStatus(PaymentStatus.CANCELLED);

                    order.getUser().setTotalPrice(order.getUser().getTotalPrice().subtract(order.getTotalPrice()));
                    order.getUser().setPoint(order.getUser().getPoint()
                            .subtract(order.getPointsToEarn())
                            .add(order.getPointsToUse()));
                }
                default -> throw new IllegalStateException("예상치 못한 결제 상태: " + payment.getStatus());
            }
        } else {
            throw new IllegalStateException("결제 상태가 일치하지 않습니다.");
        }

        orderRepository.save(order);

        return SuccessResponse.of();
    }
}