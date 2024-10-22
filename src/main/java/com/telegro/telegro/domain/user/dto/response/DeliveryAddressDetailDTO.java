package com.telegro.telegro.domain.user.dto.response;

import com.telegro.telegro.domain.user.entity.DeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DeliveryAddressDetailDTO (
        @Schema(description = "배송지 별명")
        String name,
        @Schema(description = "배송지 주소")
        String address,
        @Schema(description = "배송지 상세 주소")
        String addressDetail,
        @Schema(description = "배송지 우편 번호")
        String zipcode
){
        public static DeliveryAddressDetailDTO of(DeliveryAddress deliveryAddress) {
                return DeliveryAddressDetailDTO.builder()
                        .name(deliveryAddress.getName())
                        .address(deliveryAddress.getAddress())
                        .addressDetail(deliveryAddress.getAddressDetail())
                        .zipcode(deliveryAddress.getZipcode())
                        .build();
        }
}
