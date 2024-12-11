package com.telegro.telegro.domain.payment.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.payment.dto.request.PaymentRequestDTO;
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
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Slf4j
public class PaymentController implements PaymentControllerDocs{

    private final HttpSession httpSession;
    private final CartRepository cartRepository;
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

    @PostMapping("/order/payment/{imp_uid}")
    public IamportResponse<Payment> validateIamport(Long id, String imp_uid, PaymentRequestDTO request) throws IamportResponseException,IOException {

        IamportResponse<Payment> payment = iamportClient.paymentByImpUid(imp_uid);

        log.info("결제 요청 응답. 결제 내역 - 주문 번호: {}", payment.getResponse().getMerchantUid());

        paymentService.processPaymentDone(id, request, imp_uid);

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
 }
