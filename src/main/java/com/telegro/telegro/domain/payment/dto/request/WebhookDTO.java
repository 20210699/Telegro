package com.telegro.telegro.domain.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDTO {
    private String imp_uid;
    private String merchant_uid;
    private String status;
    private String cancellation_id;
}
