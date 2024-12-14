package com.telegro.telegro.domain.payment.controller;

import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.telegro.telegro.domain.payment.dto.request.PaymentRequestDTO;
import com.telegro.telegro.domain.payment.dto.request.WebhookDTO;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

public interface PaymentControllerDocs {
    @Operation(summary = "결제 정보 저장", description = "결제 정보를 저장한다.")
    @ApiResponse(responseCode = "200", description = "결제 정보 저장 성공")
    public IamportResponse<Payment> validateIamport(@LoginInfo Long id, @PathVariable String imp_uid, @RequestBody PaymentRequestDTO request) throws IamportResponseException, IOException;

    @Operation(summary = "결제 취소", description = "결제를 취소하고, 주문 상태를 변경한다")
    @ApiResponse(responseCode = "200", description = "결제 취소 성공")
    public SuccessResponse<?> cancelPayment(@LoginInfo Long id, @PathVariable Long orderId) throws IamportResponseException, IOException;

//    @Operation(summary = "주문 정보 삭제", description = "결제 완료 화면에서 세션 저장값, 장바구니 삭제한다.")
//    @ApiResponse(responseCode = "200", description = "주문 정보 삭제 성공")
//    public void deleteSession();

    @Operation(summary = "결제 상태 변경", description = "웹훅 수신을 받고 결제 검증 과정 거친 후 결제 상태를 변경한다.")
    @ApiResponse(responseCode = "200", description = "결제 정보 변경 성공")
    public void updatePaymentStatus(@RequestBody WebhookDTO request) throws IamportResponseException, IOException;
}
