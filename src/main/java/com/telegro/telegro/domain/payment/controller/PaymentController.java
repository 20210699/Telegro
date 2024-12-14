package com.telegro.telegro.domain.payment.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.order.repository.OrderRepository;
import com.telegro.telegro.domain.payment.dto.request.PaymentRequestDTO;
import com.telegro.telegro.domain.payment.dto.request.WebhookDTO;
import com.telegro.telegro.domain.payment.service.PaymentService;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class PaymentController implements PaymentControllerDocs{

    private final HttpSession httpSession;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
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

//    @PostMapping("api/payments/{imp_uid}")
    @PostMapping("api/v1/order/payment/{imp_uid}")
    public IamportResponse<Payment> validateIamport(Long id, String imp_uid, PaymentRequestDTO request) throws IamportResponseException,IOException {

        IamportResponse<Payment> payment = iamportClient.paymentByImpUid(imp_uid);

        paymentService.processPaymentDone(id, request, imp_uid);

        return payment; // Todo  : 주문 완료 화면에 맞는 DTO 생성
    }

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

        IamportResponse<Payment> payment = iamportClient.cancelPaymentByImpUid(cancelData);

        return SuccessResponse.of();
    }

    /*@GetMapping("/order/paymentconfirm")
    public void deleteSession() {
        List<Long>cartIds = (List<Long>) httpSession.getAttribute("cartIds");

        for(Long cartId : cartIds){
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> CustomException.of(Error.CART_NOT_FOUND));

            cartRepository.delete(cart);
        }
        httpSession.removeAttribute("temporaryOrder");
        httpSession.removeAttribute("cartIds");
    }*/ // Todo : 세션 정보 삭제 로직 어떻게 처리?

    @Transactional
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

            Order savedOrder = orderRepository.save(order);
            log.info("성공적으로 상태 변경 : {}", savedOrder.getOrderStatus().toString());
        } else {
            throw new IllegalStateException("결제 상태가 일치하지 않습니다.");
        }
    }
}
