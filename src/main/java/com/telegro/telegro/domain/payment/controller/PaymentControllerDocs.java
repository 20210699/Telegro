package com.telegro.telegro.domain.payment.controller;

import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.telegro.telegro.domain.payment.dto.request.PaymentRequestDTO;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

//Todo : Payment 관련 추후 리팩토링
public interface PaymentControllerDocs {
    @Operation(summary = "결제 정보 저장", description = "결제 정보를 저장한다.")
    @ApiResponse(responseCode = "200", description = "결제 정보 저장 성공")
    public IamportResponse<Payment> validateIamport(@LoginInfo Long id, @PathVariable String imp_uid, @RequestBody PaymentRequestDTO request) throws IamportResponseException, IOException;

    @Operation(summary = "주문 정보 삭제", description = "결제 완료 화면에서 세션 저장값, 장바구니 삭제한다.")
    @ApiResponse(responseCode = "200", description = "주문 정보 삭제 성공")
    public void deleteSession();
}
