package com.telegro.telegro.domain.payment.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.order.repository.OrderRepository;
import com.telegro.telegro.domain.payment.dto.request.PaymentRequestDTO;
import com.telegro.telegro.domain.payment.dto.request.WebhookDTO;
import com.telegro.telegro.domain.payment.service.PaymentService;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
//Todo : url 통일성 있게 수정
public class PaymentController implements PaymentControllerDocs{

    private final HttpSession httpSession;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private IamportClient iamportClient;
    private final PaymentService paymentService;

    @Value("${imp.api.apikey}")
    private String apiKey;

    @Value("${imp.api.secretkey}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    @PostMapping("api/v1/order/payment/{imp_uid}")
    public IamportResponse<Payment> validateIamport(Long id, String imp_uid, PaymentRequestDTO request) throws IamportResponseException,IOException {

        IamportResponse<Payment> payment = iamportClient.paymentByImpUid(imp_uid);

        log.info("결제 요청 응답. 결제 내역 - 주문 번호: {}", payment.getResponse().getMerchantUid());

        paymentService.processPaymentDone(id, request, imp_uid);

        return payment;
    }

    @PostMapping("api/v1/{orderId}")
    public IamportResponse<Payment> cancelPayment(Long id, Long orderId) throws IamportResponseException, IOException {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.of(Error.ORDER_NOT_FOUND));

        // Todo : 관리자 혹은 주문자만 결제 취소 가능

        CancelData cancelData = new CancelData(order.getOrderNumber(), true);

        IamportResponse<Payment> payment = iamportClient.cancelPaymentByImpUid(cancelData);

        // Todo : 결제 취소 시 주문, 결제 상태 변경(웹훅으로 상태 관리)

        return payment;
    }

    @GetMapping("/order/paymentconfirm")
    public void deleteSession() {
        List<Long>cartIds = (List<Long>) httpSession.getAttribute("cartIds");

        for(Long cartId : cartIds){
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> CustomException.of(Error.CART_NOT_FOUND));

            cartRepository.delete(cart);
        }
        httpSession.removeAttribute("temporaryOrder");
        httpSession.removeAttribute("cartIds");
    }

    @PostMapping("/payments/update")
    public void updatePaymentStatus(WebhookDTO request) throws IamportResponseException, IOException {

        String paymentStatus = iamportClient.paymentByImpUid(request.getImp_uid()).getResponse().getStatus();

        if (request.getStatus().equals(paymentStatus)) {
            Order order = orderRepository.findByOrderNumber(request.getImp_uid())
                    .orElseThrow(() -> CustomException.of(Error.ORDER_NOT_FOUND));

            log.info("orderNum : {}", order.getOrderNumber());

            switch (paymentStatus) {
                case "paid":
                    order.setOrderStatus(OrderStatus.PAYMENT_COMPLETED);
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    break;
                case "ready":
                    order.setOrderStatus(OrderStatus.ORDER_COMPLETED);
                    order.setPaymentStatus(PaymentStatus.PENDING);
                    break;
                case "cancelled":
                    order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
                    order.setPaymentStatus(PaymentStatus.CANCELLED);
                    break;
                default:
                    throw new IllegalStateException("예상치 못한 결제 상태 : " + paymentStatus);
            }
            log.info("성공적으로 상태 변경 : {}", order.getOrderStatus().toString());
        } else {
            throw new IllegalStateException("결제 상태가 일치하지 않습니다.");
        }
    }
}
