package com.telegro.telegro.domain.payment.controller;

import com.siot.IamportRestClient.exception.IamportResponseException;
import com.telegro.telegro.domain.payment.dto.request.WebhookDTO;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

public interface PaymentControllerDocs {
    @Operation(summary = "결제 검증", description = "결제 검증 후 결제 확정을 한다.")
    @ApiResponse(responseCode = "200", description = "결제 확정 성공")
    public SuccessResponse<?> validatePayment(@PathVariable String imp_uid) throws IamportResponseException, IOException;


    @Operation(summary = "결제 취소", description = "결제를 취소하고, 주문 상태를 변경한다")
    @ApiResponse(responseCode = "200", description = "결제 취소 성공")
    public SuccessResponse<?> cancelPayment(@LoginInfo Long id, @PathVariable Long orderId) throws IamportResponseException, IOException;

    @Operation(summary = "결제 검증 및 결제 상태 변경", description = "웹훅 수신을 받고 결제 검증 과정 거친 후 결제 상태를 변경한다.")
    @ApiResponse(responseCode = "200", description = "결제 정보 변경 성공")
    public SuccessResponse<?> updatePaymentStatus(@RequestBody WebhookDTO request) throws IamportResponseException, IOException;
}
