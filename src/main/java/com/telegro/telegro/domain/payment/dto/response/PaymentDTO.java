package com.telegro.telegro.domain.payment.dto.response;

import com.siot.IamportRestClient.response.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PaymentDTO(
        @Schema(description = "결제 수단")
        String paymentMethod,
        @Schema(description = "매출 전표/취소 영수증")
        String receipt_url,
        @Schema(description = "현금 영수증 발행 여부")
        boolean cash_receipt_issued // Todo : 현금 영수증 발행이 true면 현금 영수증 url 찾아주기
) {
        public static PaymentDTO of(Payment payment) {
                return PaymentDTO.builder()
                        .paymentMethod(payment.getPayMethod())
                        .receipt_url(payment.getReceiptUrl())
                        .cash_receipt_issued(payment.isCashReceiptIssued())
                        .build();
        }
}
